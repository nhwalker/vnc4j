package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.messages.*;

import io.github.nhwalker.vnc4j.protocol.internal.ClientMessageDispatch;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A TCP socket server that implements the required parts of the RFB 3.8
 * protocol handshake and message loop.
 *
 * <p>For each accepted connection the server:
 * <ol>
 *   <li>Creates a {@link VncSocketServerHandle} backed by the socket's output
 *       stream and passes it to the supplied {@link VncServerFactory}.</li>
 *   <li>Runs the RFB handshake (version exchange, security-type negotiation
 *       with "None" security, {@link ClientInit}/{@link ServerInit}).</li>
 *   <li>Enters a message loop that reads incoming {@link ClientMessage}
 *       instances and dispatches them to the {@link VncServer}.</li>
 * </ol>
 *
 * <p>Only security type 1 (None) is advertised and accepted. Connections that
 * request any other security type are rejected with a failure result.
 *
 * <p>Each connection runs on its own thread drawn from an internal cached
 * thread pool. The accept loop itself also runs on a pool thread after
 * {@link #start()} is called.
 *
 * <p>This class is thread-safe.
 */
public class VncSocketServer implements Closeable {

    private static final int SECURITY_TYPE_NONE = 1;

    private final VncServerFactory factory;
    private final ServerSocket serverSocket;
    private final ExecutorService executor;
    private volatile boolean running;

    /**
     * Creates a server bound to the given port on all interfaces.
     *
     * @param port    the TCP port to listen on (0 for an ephemeral port)
     * @param factory the factory used to create a {@link VncServer} per
     *                connection
     * @throws IOException if the server socket cannot be created
     */
    public VncSocketServer(int port, VncServerFactory factory) throws IOException {
        this(new InetSocketAddress(port), factory);
    }

    /**
     * Creates a server bound to the given address and port.
     *
     * @param address the local address and port to bind to
     * @param factory the factory used to create a {@link VncServer} per
     *                connection
     * @throws IOException if the server socket cannot be created
     */
    public VncSocketServer(InetSocketAddress address, VncServerFactory factory) throws IOException {
        this.factory = factory;
        this.serverSocket = new ServerSocket(address.getPort(), 50, address.getAddress());
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "vnc-server");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Returns the local port this server is listening on. Useful when the
     * server was constructed with port {@code 0}.
     *
     * @return the bound local port
     */
    public int getLocalPort() {
        return serverSocket.getLocalPort();
    }

    /**
     * Starts the accept loop in a background thread. This method returns
     * immediately.
     */
    public void start() {
        running = true;
        executor.submit(this::acceptLoop);
    }

    /**
     * Stops accepting new connections and closes the server socket. In-flight
     * connections are not forcibly terminated.
     *
     * @throws IOException if closing the server socket fails
     */
    @Override
    public void close() throws IOException {
        running = false;
        serverSocket.close();
        executor.shutdown();
    }

    // -------------------------------------------------------------------------
    // Accept loop
    // -------------------------------------------------------------------------

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                executor.submit(() -> handleConnection(socket));
            } catch (IOException e) {
                if (running) {
                    // Log or surface if needed; don't crash the loop.
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Per-connection handling
    // -------------------------------------------------------------------------

    private void handleConnection(Socket socket) {
        try (socket) {
            socket.setTcpNoDelay(true);
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            HandleImpl handle = new HandleImpl(out, socket);
            VncServer server = factory.create(handle);
            try {
                performHandshake(in, out, server);
                messageLoop(in, server);
            } finally {
                server.onClose();
            }
        } catch (IOException ignored) {
            // Connection closed or reset; onClose() was already called above.
        }
    }

    private void performHandshake(InputStream in, OutputStream out, VncServer server)
            throws IOException {
        // 1. Server → client: protocol version
        ProtocolVersion.newBuilder().major(3).minor(8).build().write(out);
        out.flush();

        // 2. Client → server: protocol version (accepted as-is)
        ProtocolVersion.read(in);

        // 3. Server → client: supported security types (None only)
        SecurityTypes.newBuilder()
                .securityTypes(List.of(SECURITY_TYPE_NONE))
                .build()
                .write(out);
        out.flush();

        // 4. Client → server: security type selection
        SecurityTypeSelection selection = SecurityTypeSelection.read(in);
        if (selection.securityType() != SECURITY_TYPE_NONE) {
            SecurityResult.newBuilder()
                    .status(1)
                    .failureReason("Only security type None (1) is supported")
                    .build()
                    .write(out);
            out.flush();
            throw new IOException("Client requested unsupported security type: "
                    + selection.securityType());
        }

        // 5. Server → client: security result OK (no challenge for None)
        SecurityResult.newBuilder().status(0).failureReason("").build().write(out);
        out.flush();

        // 6. Client → server: ClientInit
        ClientInit clientInit = ClientInit.read(in);

        // 7. VncServer provides ServerInit; server → client
        ServerInit serverInit = server.onClientInit(clientInit);
        serverInit.write(out);
        out.flush();
    }

    private void messageLoop(InputStream in, VncServer server) throws IOException {
        while (true) {
            ClientMessage msg;
            try {
                msg = ClientMessageDispatch.read(in);
            } catch (EOFException e) {
                break; // client disconnected cleanly
            } catch (UnsupportedOperationException e) {
                // Unknown/unsupported message type; cannot recover stream sync.
                break;
            }

            switch (msg) {
                case SetPixelFormat m -> server.onSetPixelFormat(m);
                case SetEncodings m -> server.onSetEncodings(m);
                case FramebufferUpdateRequest m -> server.onFramebufferUpdateRequest(m);
                case KeyEvent m -> server.onKeyEvent(m);
                case PointerEvent m -> server.onPointerEvent(m);
                case ClientCutText m -> server.onClientCutText(m);
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

    private static final class HandleImpl implements VncSocketServerHandle {

        private final OutputStream out;
        private final Socket socket;

        HandleImpl(OutputStream out, Socket socket) {
            this.out = out;
            this.socket = socket;
        }

        @Override
        public synchronized void send(ServerMessage message) throws IOException {
            message.write(out);
            out.flush();
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }
}
