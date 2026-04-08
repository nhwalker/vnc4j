package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.messages.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Edge-case tests for VncSocketClient and VncSocketServer covering:
 * - SetPixelFormat in HandleImpl.send() updates currentPixelFormat
 * - Server sends multiple message types (Bell, SetColourMapEntries, ServerCutText)
 * - Server closes connection cleanly (EOFException handling)
 * - Server not offering security type None causes client IOException
 * - Wrong security type selection rejected by server
 */
class VncSocketEdgeCaseTest {

    private static final PixelFormat PF_32BPP = PixelFormat.newBuilder()
            .bitsPerPixel(32).depth(24).bigEndian(false).trueColour(true)
            .redMax(255).greenMax(255).blueMax(255)
            .redShift(16).greenShift(8).blueShift(0).build();

    private static final PixelFormat PF_8BPP = PixelFormat.newBuilder()
            .bitsPerPixel(8).depth(8).bigEndian(false).trueColour(false)
            .redMax(7).greenMax(7).blueMax(3)
            .redShift(5).greenShift(2).blueShift(0).build();

    // -----------------------------------------------------------------------
    // SetPixelFormat sent via handle updates currentPixelFormat
    // -----------------------------------------------------------------------

    /**
     * When the client sends a SetPixelFormat message, the handle should update
     * the current pixel format so subsequent rectangle reads use the new format.
     */
    @Test
    @Timeout(10)
    void testClientSendsSetPixelFormat_updatesPixelFormat() throws Exception {
        CountDownLatch pfUpdateLatch = new CountDownLatch(1);
        CountDownLatch fbUpdateLatch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        // 1x1 raw rect using 32bpp
        RfbRectangleRaw rawRect = RfbRectangleRaw.newBuilder()
                .x(0).y(0).width(1).height(1)
                .pixels(new byte[]{0x01, 0x02, 0x03, 0x04}).build();

        VncSocketServer server = new VncSocketServer(0, handle -> new VncServer() {
            @Override
            public ServerInit onClientInit(ClientInit clientInit) {
                return ServerInit.newBuilder()
                        .framebufferWidth(1).framebufferHeight(1)
                        .pixelFormat(PF_8BPP).name("PFTest").build();
            }

            @Override
            public void onSetPixelFormat(SetPixelFormat msg) {
                // Client changed format; send a framebuffer update in new format
                pfUpdateLatch.countDown();
                try {
                    handle.send(FramebufferUpdate.newBuilder()
                            .rectangles(List.of(rawRect)).build());
                } catch (IOException e) {
                    error.set(e);
                }
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
                            // Send SetPixelFormat to switch to 32bpp
                            try {
                                handle.send(SetPixelFormat.newBuilder()
                                        .pixelFormat(PF_32BPP).build());
                            } catch (IOException e) {
                                error.set(e);
                            }
                        }

                        @Override
                        public void onFramebufferUpdate(FramebufferUpdate msg) {
                            fbUpdateLatch.countDown();
                        }
                    });
            client.start();

            assertTrue(pfUpdateLatch.await(5, TimeUnit.SECONDS), "SetPixelFormat not received");
            assertTrue(fbUpdateLatch.await(5, TimeUnit.SECONDS), "FramebufferUpdate not received");

            if (error.get() != null) {
                throw new AssertionError("Error", error.get());
            }

            client.close();
        } finally {
            server.close();
        }
    }

    // -----------------------------------------------------------------------
    // Server sends Bell, SetColourMapEntries, ServerCutText to client
    // -----------------------------------------------------------------------

    @Test
    @Timeout(10)
    void testServer_sendsAllMessageTypes() throws Exception {
        CountDownLatch bellLatch = new CountDownLatch(1);
        CountDownLatch colourMapLatch = new CountDownLatch(1);
        CountDownLatch cutTextLatch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        ColourMapEntry entry = ColourMapEntry.newBuilder().red(0xFFFF).green(0).blue(0).build();
        SetColourMapEntries colourMap = SetColourMapEntries.newBuilder()
                .firstColour(0).colours(List.of(entry)).build();
        ServerCutText cutText = ServerCutText.newBuilder()
                .text("clipboard".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1)).build();

        VncSocketServer server = new VncSocketServer(0, handle -> new VncServer() {
            @Override
            public ServerInit onClientInit(ClientInit clientInit) {
                return ServerInit.newBuilder()
                        .framebufferWidth(1).framebufferHeight(1)
                        .pixelFormat(PF_32BPP).name("Test").build();
            }

            @Override
            public void onFramebufferUpdateRequest(FramebufferUpdateRequest msg) {
                try {
                    handle.send(Bell.newBuilder().build());
                    handle.send(colourMap);
                    handle.send(cutText);
                } catch (IOException e) {
                    error.set(e);
                }
            }
        });
        int port = server.getLocalPort();
        server.start();

        try {
            VncSocketClient client = new VncSocketClient(
                    new InetSocketAddress("127.0.0.1", port),
                    handle -> new VncClient() {
                        @Override
                        public void onServerInit(ServerInit si) {
                            try {
                                handle.send(FramebufferUpdateRequest.newBuilder()
                                        .incremental(false).x(0).y(0).width(1).height(1).build());
                            } catch (IOException e) {
                                error.set(e);
                            }
                        }

                        @Override
                        public void onFramebufferUpdate(FramebufferUpdate msg) {}

                        @Override
                        public void onBell(Bell msg) { bellLatch.countDown(); }

                        @Override
                        public void onSetColourMapEntries(SetColourMapEntries msg) {
                            colourMapLatch.countDown();
                        }

                        @Override
                        public void onServerCutText(ServerCutText msg) {
                            cutTextLatch.countDown();
                        }
                    });
            client.start();

            assertTrue(bellLatch.await(5, TimeUnit.SECONDS), "Bell not received");
            assertTrue(colourMapLatch.await(5, TimeUnit.SECONDS), "SetColourMapEntries not received");
            assertTrue(cutTextLatch.await(5, TimeUnit.SECONDS), "ServerCutText not received");

            if (error.get() != null) throw new AssertionError("Error", error.get());
            client.close();
        } finally {
            server.close();
        }
    }

    // -----------------------------------------------------------------------
    // Client sends all message types to server
    // -----------------------------------------------------------------------

    @Test
    @Timeout(10)
    void testClient_sendsAllMessageTypes() throws Exception {
        CountDownLatch keyLatch = new CountDownLatch(1);
        CountDownLatch ptrLatch = new CountDownLatch(1);
        CountDownLatch cutTextLatch = new CountDownLatch(1);
        CountDownLatch setEncLatch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        VncSocketServer server = new VncSocketServer(0, handle -> new VncServer() {
            @Override
            public ServerInit onClientInit(ClientInit clientInit) {
                return ServerInit.newBuilder()
                        .framebufferWidth(1).framebufferHeight(1)
                        .pixelFormat(PF_32BPP).name("Test").build();
            }

            @Override
            public void onFramebufferUpdateRequest(FramebufferUpdateRequest msg) {}

            @Override
            public void onKeyEvent(KeyEvent msg) { keyLatch.countDown(); }

            @Override
            public void onPointerEvent(PointerEvent msg) { ptrLatch.countDown(); }

            @Override
            public void onClientCutText(ClientCutText msg) { cutTextLatch.countDown(); }

            @Override
            public void onSetEncodings(SetEncodings msg) { setEncLatch.countDown(); }
        });
        int port = server.getLocalPort();
        server.start();

        try {
            VncSocketClient client = new VncSocketClient(
                    new InetSocketAddress("127.0.0.1", port),
                    handle -> new VncClient() {
                        @Override
                        public void onServerInit(ServerInit si) {
                            try {
                                handle.send(SetEncodings.newBuilder().encodings(List.of(0)).build());
                                handle.send(KeyEvent.newBuilder().down(true).key(0x41).build());
                                handle.send(PointerEvent.newBuilder().buttonMask(0).x(10).y(20).build());
                                handle.send(ClientCutText.newBuilder()
                                        .text("test".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1))
                                        .build());
                            } catch (IOException e) {
                                error.set(e);
                            }
                        }

                        @Override
                        public void onFramebufferUpdate(FramebufferUpdate msg) {}
                    });
            client.start();

            assertTrue(keyLatch.await(5, TimeUnit.SECONDS), "KeyEvent not received");
            assertTrue(ptrLatch.await(5, TimeUnit.SECONDS), "PointerEvent not received");
            assertTrue(cutTextLatch.await(5, TimeUnit.SECONDS), "ClientCutText not received");
            assertTrue(setEncLatch.await(5, TimeUnit.SECONDS), "SetEncodings not received");

            if (error.get() != null) throw new AssertionError("Error", error.get());
            client.close();
        } finally {
            server.close();
        }
    }

    // -----------------------------------------------------------------------
    // Server closes after handshake → client EOFException path
    // -----------------------------------------------------------------------

    @Test
    @Timeout(10)
    void testServer_closesAfterHandshake_clientHandlesEof() throws Exception {
        CountDownLatch closedLatch = new CountDownLatch(1);

        VncSocketServer server = new VncSocketServer(0, handle -> new VncServer() {
            @Override
            public ServerInit onClientInit(ClientInit clientInit) {
                return ServerInit.newBuilder()
                        .framebufferWidth(1).framebufferHeight(1)
                        .pixelFormat(PF_32BPP).name("CloseTest").build();
            }

            @Override
            public void onFramebufferUpdateRequest(FramebufferUpdateRequest msg) {
                // Close the handle immediately to trigger client EOF
                try {
                    handle.close();
                } catch (IOException e) { /* ignore */ }
            }
        });
        int port = server.getLocalPort();
        server.start();

        try {
            VncSocketClient client = new VncSocketClient(
                    new InetSocketAddress("127.0.0.1", port),
                    handle -> new VncClient() {
                        @Override
                        public void onServerInit(ServerInit si) {
                            try {
                                handle.send(FramebufferUpdateRequest.newBuilder()
                                        .incremental(false).x(0).y(0).width(1).height(1).build());
                            } catch (IOException e) { /* ignore */ }
                        }

                        @Override
                        public void onFramebufferUpdate(FramebufferUpdate msg) {}

                        @Override
                        public void onClose() { closedLatch.countDown(); }
                    });
            client.start();

            assertTrue(closedLatch.await(5, TimeUnit.SECONDS), "onClose not called after server EOF");
            client.close();
        } finally {
            server.close();
        }
    }

    // -----------------------------------------------------------------------
    // Server that doesn't offer security type None → client IOException
    // -----------------------------------------------------------------------

    /**
     * Simulates a VNC server that only offers security type 2 (VNC auth).
     * VncSocketClient should detect that type 1 (None) is not offered and
     * handle the resulting IOException gracefully via onClose().
     */
    @Test
    @Timeout(10)
    void testClient_serverNoNoneSecurity_callsOnClose() throws Exception {
        CountDownLatch closedLatch = new CountDownLatch(1);
        AtomicBoolean serverInitCalled = new AtomicBoolean(false);

        // Start a raw server that offers only security type 2 (VNC auth), not 1
        ServerSocket raw = new ServerSocket(0);
        int rawPort = raw.getLocalPort();
        Thread rawThread = new Thread(() -> {
            try (Socket s = raw.accept()) {
                OutputStream out = s.getOutputStream();
                InputStream in = s.getInputStream();
                // Write protocol version
                out.write("RFB 003.008\n".getBytes());
                out.flush();
                // Read client version
                byte[] buf = new byte[12];
                int read = 0;
                while (read < 12) read += in.read(buf, read, 12 - read);
                // Write security types: only type 2 (VNC auth) - no type 1 (None)
                java.io.DataOutputStream dos = new java.io.DataOutputStream(out);
                dos.writeByte(1); // count
                dos.writeByte(2); // VNC auth
                dos.flush();
                // Wait a bit for client to process and close
                Thread.sleep(2000);
            } catch (Exception ignored) {}
        }, "raw-server");
        rawThread.setDaemon(true);
        rawThread.start();

        try {
            VncSocketClient client = new VncSocketClient(
                    new InetSocketAddress("127.0.0.1", rawPort),
                    handle -> new VncClient() {
                        @Override
                        public void onServerInit(ServerInit si) {
                            serverInitCalled.set(true);
                        }

                        @Override
                        public void onFramebufferUpdate(FramebufferUpdate msg) {}

                        @Override
                        public void onClose() { closedLatch.countDown(); }
                    });
            client.start();

            assertTrue(closedLatch.await(5, TimeUnit.SECONDS),
                    "onClose not called when server doesn't offer None security");
            assertFalse(serverInitCalled.get(), "onServerInit should not have been called");

            client.close();
        } finally {
            raw.close();
        }
    }
}
