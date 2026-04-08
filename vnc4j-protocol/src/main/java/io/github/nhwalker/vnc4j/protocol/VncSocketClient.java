package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.messages.*;

import io.github.nhwalker.vnc4j.protocol.internal.ServerMessageDispatch;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A TCP socket client that implements the required parts of the RFB 3.8
 * protocol handshake and message loop.
 *
 * <p>After connecting, the client:
 * <ol>
 *   <li>Creates a {@link VncSocketClientHandle} backed by the socket's output
 *       stream and passes it to the supplied {@link VncClientFactory}.</li>
 *   <li>Runs the RFB handshake (version exchange, security-type negotiation
 *       with "None" security, {@link ClientInit}/{@link ServerInit}).</li>
 *   <li>Enters a message loop that reads incoming {@link ServerMessage}
 *       instances and dispatches them to the {@link VncClient}.</li>
 * </ol>
 *
 * <p>Only security type 1 (None) is supported. If the server does not offer
 * type 1 the connection is aborted with an {@link IOException}.
 *
 * <p>The socket is connected at construction time. Call {@link #start()} to
 * begin the handshake and message loop on a background thread.
 *
 * <p>This class is thread-safe.
 */
public class VncSocketClient implements Closeable {

    private static final int SECURITY_TYPE_NONE = 1;

    private final VncClientFactory factory;
    private final Socket socket;
    private final ExecutorService executor;
    private volatile PixelFormat currentPixelFormat;
    private volatile boolean running;

    /**
     * Creates a client and connects to the given host and port.
     *
     * @param host    the VNC server hostname or IP address
     * @param port    the TCP port (typically 5900)
     * @param factory the factory used to create a {@link VncClient} for this
     *                connection
     * @throws IOException if the connection cannot be established
     */
    public VncSocketClient(String host, int port, VncClientFactory factory) throws IOException {
        this(new InetSocketAddress(host, port), factory);
    }

    /**
     * Creates a client and connects to the given address.
     *
     * @param address the remote address and port to connect to
     * @param factory the factory used to create a {@link VncClient} for this
     *                connection
     * @throws IOException if the connection cannot be established
     */
    public VncSocketClient(InetSocketAddress address, VncClientFactory factory) throws IOException {
        this.factory = factory;
        this.socket = new Socket();
        this.socket.setTcpNoDelay(true);
        this.socket.connect(address);
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "vnc-client");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Returns the remote port this client is connected to.
     *
     * @return the remote port number
     */
    public int getRemotePort() {
        return socket.getPort();
    }

    /**
     * Starts the handshake and message loop in a background thread. This
     * method returns immediately.
     */
    public void start() {
        running = true;
        executor.submit(this::runConnection);
    }

    /**
     * Closes the connection to the server.
     *
     * @throws IOException if closing the socket fails
     */
    @Override
    public void close() throws IOException {
        running = false;
        socket.close();
        executor.shutdown();
    }

    // -------------------------------------------------------------------------
    // Connection handling
    // -------------------------------------------------------------------------

    private void runConnection() {
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            HandleImpl handle = new HandleImpl(out, socket, this);
            VncClient client = factory.create(handle);
            try {
                performHandshake(in, out, client);
                messageLoop(in, client);
            } finally {
                client.onClose();
            }
        } catch (IOException ignored) {
            // Connection closed or handshake failed; onClose() was already called above.
        }
    }

    private void performHandshake(InputStream in, OutputStream out, VncClient client)
            throws IOException {
        // 1. Read server ProtocolVersion (accepted as-is)
        ProtocolVersion.read(in);

        // 2. Write client ProtocolVersion (always 3.8)
        ProtocolVersion.newBuilder().major(3).minor(8).build().write(out);
        out.flush();

        // 3. Read SecurityTypes from server
        SecurityTypes securityTypes = SecurityTypes.read(in);
        if (!securityTypes.securityTypes().contains(SECURITY_TYPE_NONE)) {
            throw new IOException(
                    "Server did not offer security type None (1); offered: "
                    + securityTypes.securityTypes());
        }

        // 4. Write SecurityTypeSelection(1) = None
        SecurityTypeSelection.newBuilder().securityType(SECURITY_TYPE_NONE).build().write(out);
        out.flush();

        // 5. Read SecurityResult; throw if not OK
        SecurityResult result = SecurityResult.read(in);
        if (result.status() != 0) {
            throw new IOException(
                    "VNC security handshake failed (status=" + result.status()
                    + "): " + result.failureReason());
        }

        // 6. Write ClientInit
        client.clientInit().write(out);
        out.flush();

        // 7. Read ServerInit; store initial pixel format; notify client
        ServerInit serverInit = ServerInit.read(in);
        this.currentPixelFormat = serverInit.pixelFormat();
        client.onServerInit(serverInit);
    }

    private void messageLoop(InputStream in, VncClient client) throws IOException {
        while (true) {
            ServerMessage msg;
            try {
                msg = ServerMessageDispatch.read(in, currentPixelFormat);
            } catch (EOFException e) {
                break; // server disconnected cleanly
            } catch (UnsupportedOperationException e) {
                // Unknown/unsupported message type; cannot recover stream sync.
                break;
            }

            switch (msg) {
                case FramebufferUpdate   m -> client.onFramebufferUpdate(m);
                case SetColourMapEntries m -> client.onSetColourMapEntries(m);
                case Bell                m -> client.onBell(m);
                case ServerCutText       m -> client.onServerCutText(m);
                default -> {
                    // Unsupported extension message; break to avoid desyncing.
                    return;
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Handle implementation
    // -------------------------------------------------------------------------

    private static final class HandleImpl implements VncSocketClientHandle {

        private final OutputStream out;
        private final Socket socket;
        private final VncSocketClient owner;

        HandleImpl(OutputStream out, Socket socket, VncSocketClient owner) {
            this.out = out;
            this.socket = socket;
            this.owner = owner;
        }

        @Override
        public synchronized void send(ClientMessage message) throws IOException {
            // Update currentPixelFormat before writing so that any FramebufferUpdate
            // arriving immediately after uses the new format. The server cannot
            // respond before it receives the message, so updating first closes
            // the race window entirely.
            if (message instanceof SetPixelFormat spf) {
                owner.currentPixelFormat = spf.pixelFormat();
            }
            message.write(out);
            out.flush();
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }
}
