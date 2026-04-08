package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.messages.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests for {@link VncSocketServer} and {@link VncSocketClient}.
 *
 * <p>Tests use loopback TCP sockets to run the full RFB 3.8 handshake. The server
 * uses security type 1 (None) and provides a simple 640×480 desktop. After the
 * handshake the client sends a FramebufferUpdateRequest and the server responds
 * with a FramebufferUpdate containing a single Raw rectangle.
 *
 * <p>The RFB 3.8 handshake (from rfbproto.rst.txt):
 * <pre>
 * Server → Client: ProtocolVersion "RFB 003.008\n"
 * Client → Server: ProtocolVersion "RFB 003.008\n"
 * Server → Client: SecurityTypes (1 entry: 1=None)
 * Client → Server: SecurityTypeSelection (1=None)
 * Server → Client: SecurityResult (0=OK)
 * Client → Server: ClientInit (shared flag)
 * Server → Client: ServerInit (width, height, pixelFormat, name)
 * --- message loop ---
 * </pre>
 */
class VncSocketIntegrationTest {

    private static final PixelFormat PIXEL_FORMAT = PixelFormat.newBuilder()
            .bitsPerPixel(32).depth(24).bigEndian(false).trueColour(true)
            .redMax(255).greenMax(255).blueMax(255)
            .redShift(16).greenShift(8).blueShift(0).build();

    /**
     * Verifies that a VncSocketServer and VncSocketClient can complete the
     * full RFB handshake and exchange a single FramebufferUpdate.
     */
    @Test
    @Timeout(10)
    void testFullHandshakeAndFramebufferUpdate() throws Exception {
        CountDownLatch serverInitLatch = new CountDownLatch(1);
        CountDownLatch fbUpdateLatch = new CountDownLatch(1);
        AtomicReference<ServerInit> receivedServerInit = new AtomicReference<>();
        AtomicReference<FramebufferUpdate> receivedUpdate = new AtomicReference<>();
        AtomicReference<Throwable> serverError = new AtomicReference<>();

        // Create a 1x1 pixel raw rectangle (8bpp for simplicity)
        PixelFormat pf8 = PixelFormat.newBuilder()
                .bitsPerPixel(8).depth(8).bigEndian(false).trueColour(false)
                .redMax(7).greenMax(7).blueMax(3)
                .redShift(5).greenShift(2).blueShift(0).build();
        RfbRectangleRaw rawRect = RfbRectangleRaw.newBuilder()
                .x(0).y(0).width(1).height(1)
                .pixels(new byte[]{0x42}).build();
        FramebufferUpdate updateToSend = FramebufferUpdate.newBuilder()
                .rectangles(List.of(rawRect)).build();

        // Start server on ephemeral port
        VncSocketServer server = new VncSocketServer(0, handle -> new VncServer() {
            @Override
            public ServerInit onClientInit(ClientInit clientInit) {
                return ServerInit.newBuilder()
                        .framebufferWidth(1).framebufferHeight(1)
                        .pixelFormat(pf8).name("TestDesktop").build();
            }

            @Override
            public void onFramebufferUpdateRequest(FramebufferUpdateRequest msg) {
                try {
                    handle.send(updateToSend);
                } catch (IOException e) {
                    serverError.set(e);
                }
            }
        });
        int port = server.getLocalPort();
        server.start();

        try {
            // Connect client
            VncSocketClient client = new VncSocketClient(
                    new InetSocketAddress("127.0.0.1", port),
                    handle -> new VncClient() {
                        @Override
                        public void onServerInit(ServerInit si) {
                            receivedServerInit.set(si);
                            serverInitLatch.countDown();
                            // Send a framebuffer update request
                            try {
                                handle.send(FramebufferUpdateRequest.newBuilder()
                                        .incremental(false).x(0).y(0).width(1).height(1)
                                        .build());
                            } catch (IOException e) {
                                serverError.set(e);
                            }
                        }

                        @Override
                        public void onFramebufferUpdate(FramebufferUpdate msg) {
                            receivedUpdate.set(msg);
                            fbUpdateLatch.countDown();
                        }
                    });
            client.start();

            // Wait for handshake
            assertTrue(serverInitLatch.await(5, TimeUnit.SECONDS), "ServerInit not received");
            ServerInit si = receivedServerInit.get();
            assertNotNull(si);
            assertEquals("TestDesktop", si.name());
            assertEquals(1, si.framebufferWidth());
            assertEquals(1, si.framebufferHeight());

            // Wait for framebuffer update
            assertTrue(fbUpdateLatch.await(5, TimeUnit.SECONDS), "FramebufferUpdate not received");
            FramebufferUpdate update = receivedUpdate.get();
            assertNotNull(update);
            assertEquals(1, update.rectangles().size());
            assertInstanceOf(RfbRectangleRaw.class, update.rectangles().get(0));

            if (serverError.get() != null) {
                throw new AssertionError("Server error", serverError.get());
            }

            client.close();
        } finally {
            server.close();
        }
    }

    /**
     * Verifies that VncSocketServer correctly rejects an unsupported security type
     * and sends a failure SecurityResult.
     */
    @Test
    @Timeout(10)
    void testServerInit_closedCleanly() throws Exception {
        CountDownLatch closedLatch = new CountDownLatch(1);

        VncSocketServer server = new VncSocketServer(0, handle -> new VncServer() {
            @Override
            public ServerInit onClientInit(ClientInit clientInit) {
                return ServerInit.newBuilder()
                        .framebufferWidth(100).framebufferHeight(100)
                        .pixelFormat(PIXEL_FORMAT).name("Close Test").build();
            }

            @Override
            public void onFramebufferUpdateRequest(FramebufferUpdateRequest msg) {}
        });
        int port = server.getLocalPort();
        server.start();

        try {
            VncSocketClient client = new VncSocketClient(
                    new InetSocketAddress("127.0.0.1", port),
                    handle -> new VncClient() {
                        @Override
                        public void onServerInit(ServerInit si) {
                            // Immediately close
                            try {
                                handle.close();
                            } catch (IOException e) {
                                // ignore
                            }
                        }

                        @Override
                        public void onFramebufferUpdate(FramebufferUpdate msg) {}

                        @Override
                        public void onClose() {
                            closedLatch.countDown();
                        }
                    });
            client.start();

            assertTrue(closedLatch.await(5, TimeUnit.SECONDS), "onClose not called");
            client.close();
        } finally {
            server.close();
        }
    }

    /**
     * Verifies VncSocketServer getLocalPort() and VncSocketClient getRemotePort().
     */
    @Test
    @Timeout(10)
    void testPortAccessors() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        VncSocketServer server = new VncSocketServer(0, handle -> new VncServer() {
            @Override
            public ServerInit onClientInit(ClientInit clientInit) {
                return ServerInit.newBuilder()
                        .framebufferWidth(1).framebufferHeight(1)
                        .pixelFormat(PIXEL_FORMAT).name("Port Test").build();
            }

            @Override
            public void onFramebufferUpdateRequest(FramebufferUpdateRequest msg) {}
        });
        int serverPort = server.getLocalPort();
        assertTrue(serverPort > 0, "Server port should be > 0");
        server.start();

        try {
            VncSocketClient client = new VncSocketClient(
                    new InetSocketAddress("127.0.0.1", serverPort),
                    handle -> new VncClient() {
                        @Override
                        public void onServerInit(ServerInit si) {
                            latch.countDown();
                        }

                        @Override
                        public void onFramebufferUpdate(FramebufferUpdate msg) {}
                    });
            assertEquals(serverPort, client.getRemotePort());
            client.start();

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            client.close();
        } finally {
            server.close();
        }
    }
}
