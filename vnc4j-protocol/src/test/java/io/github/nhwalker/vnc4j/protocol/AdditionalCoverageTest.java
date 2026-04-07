package io.github.nhwalker.vnc4j.protocol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional tests targeting the remaining coverage gaps:
 * - VncSocketClient: SecurityResult failure path
 * - VncSocketServer: wrong security type selection path
 * - RfbRectangleDispatch: unknown TightPng ctrl byte
 * - Various impl null branches and from() methods
 */
class AdditionalCoverageTest {

    private static final PixelFormat PF_32BPP = PixelFormat.newBuilder()
            .bitsPerPixel(32).depth(24).bigEndian(false).trueColour(true)
            .redMax(255).greenMax(255).blueMax(255)
            .redShift(16).greenShift(8).blueShift(0).build();

    // -----------------------------------------------------------------------
    // VncSocketClient: server returns SecurityResult failure
    // -----------------------------------------------------------------------

    /**
     * When the server returns a SecurityResult with status != 0, VncSocketClient
     * should throw an IOException and call {@link VncClient#onClose()}.
     *
     * <p>RFB 3.8 spec § 6.1.2.2 – SecurityResult: if status != 0 the connection fails.
     * Wire format: U32 status; if status != 0: U32 reasonLength + reasonBytes.
     *
     * <pre>
     * Server → Client: "RFB 003.008\n"
     * Client → Server: "RFB 003.008\n"
     * Server → Client: SecurityTypes (count=1, type=1 None)
     * Client → Server: SecurityTypeSelection (1=None)
     * Server → Client: SecurityResult status=1 + reason "Bad credentials"
     * → Client should detect failure and call onClose()
     * </pre>
     */
    @Test
    @Timeout(10)
    void testVncSocketClient_securityResultFailure_callsOnClose() throws Exception {
        CountDownLatch closedLatch = new CountDownLatch(1);
        AtomicBoolean serverInitCalled = new AtomicBoolean(false);

        java.net.ServerSocket raw = new java.net.ServerSocket(0);
        int rawPort = raw.getLocalPort();

        Thread serverThread = new Thread(() -> {
            try (Socket s = raw.accept()) {
                java.io.DataOutputStream dos = new java.io.DataOutputStream(s.getOutputStream());
                java.io.DataInputStream dis = new java.io.DataInputStream(s.getInputStream());

                // Write server version
                s.getOutputStream().write("RFB 003.008\n".getBytes());
                s.getOutputStream().flush();
                // Read client version
                byte[] ver = new byte[12];
                dis.readFully(ver);

                // Write SecurityTypes: count=1, type=1 (None)
                dos.writeByte(1); dos.writeByte(1); dos.flush();
                // Read SecurityTypeSelection
                dis.readUnsignedByte();

                // Write SecurityResult: status=1 (failure) + reason
                byte[] reason = "Bad credentials".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                dos.writeInt(1);           // status=1 failure
                dos.writeInt(reason.length);
                dos.write(reason);
                dos.flush();

                Thread.sleep(2000);
            } catch (Exception ignored) {}
        }, "raw-server-sec-fail");
        serverThread.setDaemon(true);
        serverThread.start();

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
                    "onClose not called after SecurityResult failure");
            assertFalse(serverInitCalled.get(), "onServerInit should not be called after security failure");
            client.close();
        } finally {
            raw.close();
        }
    }

    // -----------------------------------------------------------------------
    // VncSocketServer: client sends wrong security type selection
    // -----------------------------------------------------------------------

    /**
     * When the client selects a security type other than 1 (None), VncSocketServer
     * should send a failure SecurityResult, throw an IOException, and call
     * {@link VncServer#onClose()}.
     *
     * <p>RFB 3.8 spec § 6.1.2.1 – Security Type: server rejects unsupported selection.
     *
     * <pre>
     * Server → Client: "RFB 003.008\n"
     * Client → Server: "RFB 003.008\n"
     * Server → Client: SecurityTypes (count=1, type=1 None)
     * Client → Server: SecurityTypeSelection (2=VNC auth — unsupported)
     * Server → Client: SecurityResult status=1 + reason
     * → Server calls onClose()
     * </pre>
     */
    @Test
    @Timeout(10)
    void testVncSocketServer_wrongSecurityType_callsOnClose() throws Exception {
        CountDownLatch serverClosedLatch = new CountDownLatch(1);
        AtomicBoolean onClientInitCalled = new AtomicBoolean(false);

        VncSocketServer server = new VncSocketServer(0, handle -> new VncServer() {
            @Override
            public ServerInit onClientInit(ClientInit clientInit) {
                onClientInitCalled.set(true);
                return ServerInit.newBuilder()
                        .framebufferWidth(1).framebufferHeight(1)
                        .pixelFormat(PF_32BPP).name("WrongSecTest").build();
            }
            @Override
            public void onFramebufferUpdateRequest(FramebufferUpdateRequest msg) {}
            @Override
            public void onClose() { serverClosedLatch.countDown(); }
        });
        int port = server.getLocalPort();
        server.start();

        Thread clientThread = new Thread(() -> {
            try (Socket s = new Socket("127.0.0.1", port)) {
                java.io.DataInputStream dis = new java.io.DataInputStream(s.getInputStream());
                java.io.DataOutputStream dos = new java.io.DataOutputStream(s.getOutputStream());

                // Read server version, send client version
                byte[] ver = new byte[12];
                dis.readFully(ver);
                dos.write("RFB 003.008\n".getBytes()); dos.flush();

                // Read SecurityTypes: count + types
                int count = dis.readUnsignedByte();
                for (int i = 0; i < count; i++) dis.readUnsignedByte();

                // Send wrong security type selection: 2 (VNC auth, not supported)
                dos.writeByte(2); dos.flush();

                // Server should respond with failure SecurityResult
                Thread.sleep(2000);
            } catch (Exception ignored) {}
        }, "raw-client-wrong-sec");
        clientThread.setDaemon(true);
        clientThread.start();

        try {
            assertTrue(serverClosedLatch.await(5, TimeUnit.SECONDS),
                    "VncServer.onClose() not called after wrong security type");
            assertFalse(onClientInitCalled.get(), "onClientInit should not be called");
        } finally {
            server.close();
        }
    }

    // -----------------------------------------------------------------------
    // RfbRectangleDispatch: unknown TightPng ctrl byte
    // -----------------------------------------------------------------------

    /**
     * TightPng sub-dispatch (encoding -260) handles ctrl high nibble 0x8 (fill),
     * 0x9 (JPEG), 0xA (PNG). Any other high nibble throws
     * {@link UnsupportedOperationException}.
     *
     * <p>Wire format: 10-byte rect header (x, y, w, h, enc-type S32), then ctrl byte.
     * The high nibble of the ctrl byte selects fill/jpeg/png/unknown.
     */
    @Test
    void testRfbRectangleDispatch_unknownTightPngCtrl_throwsUOE() throws IOException {
        // Build a rectangle header manually for encoding type -260 (TightPng)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);
        dos.writeShort(0);     // x
        dos.writeShort(0);     // y
        dos.writeShort(4);     // width
        dos.writeShort(4);     // height
        dos.writeInt(-260);    // encoding type = TightPng
        // ctrl byte with high nibble = 0x7 → not 0x8/0x9/0xA → UnsupportedOperationException
        dos.writeByte(0x70);
        byte[] bytes = baos.toByteArray();

        assertThrows(UnsupportedOperationException.class,
                () -> RfbRectangle.read(new ByteArrayInputStream(bytes), PF_32BPP));
    }

    // -----------------------------------------------------------------------
    // RfbRectangleZlib / Zrle: from() builder method
    // -----------------------------------------------------------------------

    @Test
    void testRfbRectangleZlib_builderFrom() {
        RfbRectangleZlib orig = RfbRectangleZlib.newBuilder()
                .x(1).y(2).width(3).height(4).zlibData(new byte[]{0x42}).build();
        RfbRectangleZlib copy = RfbRectangleZlib.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testRfbRectangleZrle_builderFrom() {
        RfbRectangleZrle orig = RfbRectangleZrle.newBuilder()
                .x(0).y(0).width(4).height(4).zlibData(new byte[]{0x11}).build();
        RfbRectangleZrle copy = RfbRectangleZrle.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    // -----------------------------------------------------------------------
    // Various Impl: remaining null branches
    // -----------------------------------------------------------------------

    /**
     * RfbRectangleRaw.write() when pixels is null: the write includes the
     * null check {@code if (pixels != null) dos.write(pixels)}.
     */
    @Test
    void testRfbRectangleRaw_nullPixels_write() throws IOException {
        RfbRectangleRaw msg = RfbRectangleRaw.newBuilder()
                .x(0).y(0).width(1).height(1).pixels(null).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> msg.write(baos));
        // 2+2+2+2 header + 4 enc-type = 12 bytes; no pixels written when null
        assertEquals(12, baos.size());
    }

    /**
     * Tests that the RfbRectangleHextile read method handles a round-trip
     * with FOREGROUND_SPECIFIED and SUBRECTS_COLOURED simultaneously.
     *
     * <p>RFB spec § 6.7.5: subencoding bits are flags that can be combined.
     * When FOREGROUND_SPECIFIED is set, a foreground pixel follows the bg pixel;
     * when SUBRECTS_COLOURED is also set, each subrect overrides both.
     */
    @Test
    void testHextileTile_fgSpecified_and_subrects_coloured() throws IOException {
        HextileSubrect sr = HextileSubrect.newBuilder()
                .pixel(new byte[]{0x77}).x(1).y(1).width(2).height(2).build();
        int subenc = HextileTile.SUBENC_BACKGROUND_SPECIFIED
                | HextileTile.SUBENC_FOREGROUND_SPECIFIED
                | HextileTile.SUBENC_ANY_SUBRECTS
                | HextileTile.SUBENC_SUBRECTS_COLOURED;
        HextileTile orig = HextileTile.newBuilder()
                .subencoding(subenc)
                .background(new byte[]{0x11})
                .foreground(new byte[]{0x22})
                .subrects(List.of(sr))
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        orig.write(baos);
        HextileTile copy = HextileTile.read(
                new ByteArrayInputStream(baos.toByteArray()), 8, 8, 1);
        assertEquals(subenc, copy.subencoding());
        assertEquals(1, copy.subrects().size());
        assertArrayEquals(new byte[]{0x77}, copy.subrects().get(0).pixel());
    }

    // -----------------------------------------------------------------------
    // SecurityResult: failure path read round-trip
    // -----------------------------------------------------------------------

    /**
     * SecurityResult with status=2 (Too Many) — no failure reason per spec
     * (client can distinguish 0=OK, 1=fail+reason, 2=too-many-no-reason).
     * Tests the status != 0 branch in read() that reads the failure reason.
     */
    @Test
    void testSecurityResult_failureWithReason_readRoundTrip() throws IOException {
        SecurityResult orig = SecurityResult.newBuilder()
                .status(1)
                .failureReason("Authentication failure")
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        orig.write(baos);
        SecurityResult copy = SecurityResult.read(new ByteArrayInputStream(baos.toByteArray()));
        assertEquals(1, copy.status());
        assertEquals("Authentication failure", copy.failureReason());
    }

    // -----------------------------------------------------------------------
    // ClientInit: shared=false branch
    // -----------------------------------------------------------------------

    /**
     * ClientInitImpl.write() writes a byte: shared ? 1 : 0. The shared=false
     * branch covers the 0 byte write path.
     */
    @Test
    void testClientInit_notShared_writeRead() throws IOException {
        ClientInit orig = ClientInit.newBuilder().shared(false).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        orig.write(baos);
        assertEquals(1, baos.size());
        assertEquals(0, baos.toByteArray()[0]);
        ClientInit copy = ClientInit.read(new ByteArrayInputStream(baos.toByteArray()));
        assertFalse(copy.shared());
    }
}
