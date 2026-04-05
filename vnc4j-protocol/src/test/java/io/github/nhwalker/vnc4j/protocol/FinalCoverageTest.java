package io.github.nhwalker.vnc4j.protocol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Final coverage improvements targeting:
 * - {@code default int encodingType()} on all {@link RfbRectangle} sub-interfaces
 * - {@code Builder.from()} on singleton-style message builders with no fields
 * - Null-array branches in {@code write()} methods
 * - GiiIo little-endian write/read paths
 * - HextileTile and ZlibHexTile foreground-specified paths
 * - VncSocketClient String host+port constructor
 * - VncSocketClient/Server UnsupportedOperationException path in message loop
 */
class FinalCoverageTest {

    private static final PixelFormat PF_8BPP = PixelFormat.newBuilder()
            .bitsPerPixel(8).depth(8).bigEndian(false).trueColour(false)
            .redMax(7).greenMax(7).blueMax(3)
            .redShift(5).greenShift(2).blueShift(0).build();

    private static final PixelFormat PF_32BPP = PixelFormat.newBuilder()
            .bitsPerPixel(32).depth(24).bigEndian(false).trueColour(true)
            .redMax(255).greenMax(255).blueMax(255)
            .redShift(16).greenShift(8).blueShift(0).build();

    // -----------------------------------------------------------------------
    // encodingType() default method coverage for every RfbRectangle interface
    // -----------------------------------------------------------------------

    /**
     * Each sealed RfbRectangle sub-interface has
     * {@code default int encodingType() { return ENCODING_TYPE; }}.
     * Calling this method exercises the 2 bytecode instructions that JaCoCo
     * tracks per default method body.
     *
     * <p>RFB spec § 6.7: every rectangle header carries a 32-bit {@code encoding-type}.
     */
    @Test
    void testEncodingType_allRfbRectangleInterfaces() {
        // Encoding 0 – Raw
        assertEquals(0,
                RfbRectangleRaw.newBuilder().x(0).y(0).width(1).height(1)
                        .pixels(new byte[1]).build().encodingType());

        // Encoding 1 – CopyRect
        assertEquals(1,
                RfbRectangleCopyRect.newBuilder().x(0).y(0).width(1).height(1)
                        .srcX(0).srcY(0).build().encodingType());

        // Encoding 2 – RRE
        assertEquals(2,
                RfbRectangleRre.newBuilder().x(0).y(0).width(1).height(1)
                        .background(new byte[1]).subrects(List.of()).build().encodingType());

        // Encoding 4 – CoRRE
        assertEquals(4,
                RfbRectangleCoRre.newBuilder().x(0).y(0).width(1).height(1)
                        .background(new byte[1]).subrects(List.of()).build().encodingType());

        // Encoding 5 – Hextile
        assertEquals(5,
                RfbRectangleHextile.newBuilder().x(0).y(0).width(16).height(16)
                        .tiles(List.of()).build().encodingType());

        // Encoding 6 – Zlib
        assertEquals(6,
                RfbRectangleZlib.newBuilder().x(0).y(0).width(1).height(1)
                        .zlibData(new byte[0]).build().encodingType());

        // Encoding 7 – Tight (via TightFill subtype, inherits default from RfbRectangleTight)
        assertEquals(7,
                RfbRectangleTightFill.newBuilder().x(0).y(0).width(1).height(1)
                        .streamResets(0).fillColor(new byte[]{0}).build().encodingType());

        // Encoding 8 – ZlibHex
        assertEquals(8,
                RfbRectangleZlibHex.newBuilder().x(0).y(0).width(16).height(16)
                        .tiles(List.of()).build().encodingType());

        // Encoding 16 – ZRLE
        assertEquals(16,
                RfbRectangleZrle.newBuilder().x(0).y(0).width(1).height(1)
                        .zlibData(new byte[0]).build().encodingType());

        // Encoding 21 – JPEG (no self-delimitng read; only write/builder)
        assertEquals(21,
                RfbRectangleJpeg.newBuilder().x(0).y(0).width(1).height(1)
                        .data(new byte[0]).build().encodingType());

        // Encoding 50 – H264
        assertEquals(50,
                RfbRectangleH264.newBuilder().x(0).y(0).width(16).height(16)
                        .flags(0).data(new byte[0]).build().encodingType());

        // Encoding -260 – TightPng (via TightPngFill subtype, inherits from RfbRectangleTightPng)
        assertEquals(-260,
                RfbRectangleTightPngFill.newBuilder().x(0).y(0).width(1).height(1)
                        .streamResets(0).fillColor(new byte[]{0}).build().encodingType());

        // Encoding -223 – DesktopSize
        assertEquals(-223,
                RfbRectangleDesktopSize.newBuilder().x(0).y(0).width(100).height(100)
                        .build().encodingType());

        // Encoding -224 – LastRect
        assertEquals(-224,
                RfbRectangleLastRect.newBuilder().x(0).y(0).width(0).height(0)
                        .build().encodingType());

        // Encoding -239 – Cursor
        assertEquals(-239,
                RfbRectangleCursor.newBuilder().x(0).y(0).width(1).height(1)
                        .pixels(new byte[4]).bitmask(new byte[1]).build().encodingType());

        // Encoding -240 – XCursor
        assertEquals(-240,
                RfbRectangleXCursor.newBuilder().x(0).y(0).width(0).height(0)
                        .build().encodingType());

        // Encoding -308 – ExtendedDesktopSize
        assertEquals(-308,
                RfbRectangleExtendedDesktopSize.newBuilder().x(0).y(0).width(100).height(100)
                        .screens(List.of()).build().encodingType());

        // Encoding -314 – CursorWithAlpha
        assertEquals(-314,
                RfbRectangleCursorWithAlpha.newBuilder().x(0).y(0).width(0).height(0)
                        .data(new byte[0]).build().encodingType());
    }

    // -----------------------------------------------------------------------
    // Builder.from() on singleton-style (no-field) message builders
    // -----------------------------------------------------------------------

    /**
     * Messages with no fields (Bell, EndOfContinuousUpdates, and the four QEMU audio
     * control messages) have a {@code Builder.from(msg)} default that simply returns
     * {@code this}. This exercises the 2-instruction method body.
     */
    @Test
    void testSingletonBuilderFrom() {
        Bell bell = Bell.newBuilder().build();
        assertNotNull(Bell.newBuilder().from(bell).build());

        EndOfContinuousUpdates ecu = EndOfContinuousUpdates.newBuilder().build();
        assertNotNull(EndOfContinuousUpdates.newBuilder().from(ecu).build());

        QemuAudioClientEnable qce = QemuAudioClientEnable.newBuilder().build();
        assertNotNull(QemuAudioClientEnable.newBuilder().from(qce).build());

        QemuAudioClientDisable qcd = QemuAudioClientDisable.newBuilder().build();
        assertNotNull(QemuAudioClientDisable.newBuilder().from(qcd).build());

        QemuAudioServerBegin qsb = QemuAudioServerBegin.newBuilder().build();
        assertNotNull(QemuAudioServerBegin.newBuilder().from(qsb).build());

        QemuAudioServerEnd qse = QemuAudioServerEnd.newBuilder().build();
        assertNotNull(QemuAudioServerEnd.newBuilder().from(qse).build());
    }

    // -----------------------------------------------------------------------
    // Builder.from() on fence and audio-data builders
    // -----------------------------------------------------------------------

    @Test
    void testClientFence_builderFrom() {
        ClientFence orig = ClientFence.newBuilder().flags(3).payload(new byte[]{0x01}).build();
        ClientFence copy = ClientFence.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testServerFence_builderFrom() {
        ServerFence orig = ServerFence.newBuilder().flags(7).payload(new byte[]{0x02}).build();
        ServerFence copy = ServerFence.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testQemuAudioServerData_builderFrom() {
        QemuAudioServerData orig = QemuAudioServerData.newBuilder().data(new byte[]{0x33}).build();
        QemuAudioServerData copy = QemuAudioServerData.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testRfbRectangleTightJpeg_builderFrom() {
        RfbRectangleTightJpeg orig = RfbRectangleTightJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0)
                .jpegData(new byte[]{(byte) 0xFF, (byte) 0xD8}).build();
        RfbRectangleTightJpeg copy = RfbRectangleTightJpeg.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    // -----------------------------------------------------------------------
    // Null-array branches in write() methods
    // -----------------------------------------------------------------------

    /**
     * Several write() implementations guard with {@code array != null ? array : new byte[0]}.
     * Passing {@code null} covers the otherwise-dead branch and avoids NullPointerException.
     *
     * <p>RFB wire format: these messages always write the length before the bytes,
     * so writing zero bytes is valid on the wire.
     */
    @Test
    void testWrite_nullText_clientCutText() throws IOException {
        ClientCutText msg = ClientCutText.newBuilder().text(null).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        msg.write(baos);
        // 1 type + 3 padding + 4 length = 8 bytes; length=0 so no text bytes
        assertEquals(8, baos.size());
    }

    @Test
    void testWrite_nullText_serverCutText() throws IOException {
        ServerCutText msg = ServerCutText.newBuilder().text(null).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        msg.write(baos);
        assertEquals(8, baos.size()); // 1 type + 3 padding + 4 length
    }

    @Test
    void testWrite_nullPayload_clientFence() throws IOException {
        ClientFence msg = ClientFence.newBuilder().flags(0).payload(null).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        msg.write(baos);
        // 1 type + 3 padding + 4 flags + 1 len = 9 bytes
        assertEquals(9, baos.size());
    }

    @Test
    void testWrite_nullPayload_serverFence() throws IOException {
        ServerFence msg = ServerFence.newBuilder().flags(0).payload(null).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        msg.write(baos);
        assertEquals(9, baos.size()); // 1 type + 3 padding + 4 flags + 1 len
    }

    @Test
    void testWrite_nullData_rfbRectangleZlib() throws IOException {
        RfbRectangleZlib msg = RfbRectangleZlib.newBuilder()
                .x(0).y(0).width(1).height(1).zlibData(null).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        msg.write(baos);
        // 2+2+2+2 header + 4 enc-type + 4 length = 16 bytes; length=0 so no data
        assertEquals(16, baos.size());
    }

    @Test
    void testWrite_nullData_rfbRectangleZrle() throws IOException {
        RfbRectangleZrle msg = RfbRectangleZrle.newBuilder()
                .x(0).y(0).width(1).height(1).zlibData(null).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        msg.write(baos);
        assertEquals(16, baos.size());
    }

    @Test
    void testWrite_nullData_qemuAudioServerData() throws IOException {
        QemuAudioServerData msg = QemuAudioServerData.newBuilder().data(null).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        msg.write(baos);
        // 1 type + 1 sub + 2 op + 4 length = 8 bytes
        assertEquals(8, baos.size());
    }

    @Test
    void testWrite_nullJpegData_rfbRectangleTightJpeg() throws IOException {
        RfbRectangleTightJpeg msg = RfbRectangleTightJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0).jpegData(null).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        msg.write(baos);
        // header(8) + enc(4) + ctrl(1) + compact-len(1 for 0) = 14 bytes
        assertTrue(baos.size() >= 14);
    }

    // -----------------------------------------------------------------------
    // HextileTile with FOREGROUND_SPECIFIED
    // -----------------------------------------------------------------------

    /**
     * RFB spec § 6.7.5 (Hextile) – subencoding bit 2 = ForegroundSpecified:
     * a foreground pixel value follows the (optional) background pixel.
     * Wire: subenc-byte, [background], [foreground], [subrects]
     */
    @Test
    void testHextileTile_foregroundSpecified_writeRead() throws IOException {
        int subenc = HextileTile.SUBENC_BACKGROUND_SPECIFIED
                | HextileTile.SUBENC_FOREGROUND_SPECIFIED;
        HextileTile orig = HextileTile.newBuilder()
                .subencoding(subenc)
                .background(new byte[]{0x11})
                .foreground(new byte[]{0x22})
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        orig.write(baos);
        HextileTile copy = HextileTile.read(new ByteArrayInputStream(baos.toByteArray()), 8, 8, 1);
        assertEquals(subenc, copy.subencoding());
        assertArrayEquals(new byte[]{0x11}, copy.background());
        assertArrayEquals(new byte[]{0x22}, copy.foreground());
    }

    /**
     * Null-field guard in HextileTile.write() when SUBENC_BACKGROUND_SPECIFIED is set
     * but background is null — should write nothing for background bytes without throwing.
     */
    @Test
    void testHextileTile_nullBackgroundAndForeground_noThrow() throws IOException {
        int subenc = HextileTile.SUBENC_BACKGROUND_SPECIFIED
                | HextileTile.SUBENC_FOREGROUND_SPECIFIED;
        HextileTile msg = HextileTile.newBuilder()
                .subencoding(subenc)
                .background(null)
                .foreground(null)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> msg.write(baos));
        // Only subencoding byte written when bg/fg are null
        assertEquals(1, baos.size());
    }

    /**
     * Null subrects list guard in HextileTile.write() when SUBENC_ANY_SUBRECTS is set
     * but subrects is null.
     */
    @Test
    void testHextileTile_nullSubrects_anySubrectsFlag() throws IOException {
        int subenc = HextileTile.SUBENC_ANY_SUBRECTS;
        HextileTile msg = HextileTile.newBuilder()
                .subencoding(subenc)
                .subrects(null)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> msg.write(baos));
        // subenc byte + count byte (0)
        assertEquals(2, baos.size());
    }

    /**
     * Null rawPixels guard in HextileTile.write() when SUBENC_RAW is set but rawPixels is null.
     */
    @Test
    void testHextileTile_rawSubenc_nullPixels() throws IOException {
        HextileTile msg = HextileTile.newBuilder()
                .subencoding(HextileTile.SUBENC_RAW)
                .rawPixels(null)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> msg.write(baos));
        assertEquals(1, baos.size()); // only subencoding byte
    }

    // -----------------------------------------------------------------------
    // ZlibHexTile with FOREGROUND_SPECIFIED and null-zlib paths
    // -----------------------------------------------------------------------

    /**
     * ZlibHexTile with SUBENC_BACKGROUND_SPECIFIED | SUBENC_FOREGROUND_SPECIFIED (2|4=6):
     * no zlib, no subrects — only bg and fg pixels follow the subencoding byte.
     */
    @Test
    void testZlibHexTile_foregroundSpecified_writeRead() throws IOException {
        int subenc = ZlibHexTile.SUBENC_BACKGROUND_SPECIFIED
                | ZlibHexTile.SUBENC_FOREGROUND_SPECIFIED;
        ZlibHexTile orig = ZlibHexTile.newBuilder()
                .subencoding(subenc)
                .background(new byte[]{0x11})
                .foreground(new byte[]{0x22})
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        orig.write(baos);
        ZlibHexTile copy = ZlibHexTile.read(new ByteArrayInputStream(baos.toByteArray()), 8, 8, 1);
        assertEquals(subenc, copy.subencoding());
        assertArrayEquals(new byte[]{0x11}, copy.background());
        assertArrayEquals(new byte[]{0x22}, copy.foreground());
    }

    /**
     * ZlibHexTile with SUBENC_ZLIB_RAW (32): null zlibRawData guard
     * → writes length=0.
     */
    @Test
    void testZlibHexTile_zlibRaw_nullData() throws IOException {
        ZlibHexTile msg = ZlibHexTile.newBuilder()
                .subencoding(ZlibHexTile.SUBENC_ZLIB_RAW)
                .zlibRawData(null)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> msg.write(baos));
        // subenc byte + 2-byte length (0)
        assertEquals(3, baos.size());
    }

    /**
     * ZlibHexTile with SUBENC_ANY_SUBRECTS | SUBENC_ZLIB (8|64=72): null zlibSubrectData guard
     * → writes length=0.
     */
    @Test
    void testZlibHexTile_zlibSubrects_nullData() throws IOException {
        ZlibHexTile msg = ZlibHexTile.newBuilder()
                .subencoding(ZlibHexTile.SUBENC_ANY_SUBRECTS | ZlibHexTile.SUBENC_ZLIB)
                .zlibSubrectData(null)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> msg.write(baos));
        // subenc byte + 2-byte length (0)
        assertEquals(3, baos.size());
    }

    /**
     * ZlibHexTile with null background/foreground when those flags are set.
     */
    @Test
    void testZlibHexTile_nullBgFg_noThrow() throws IOException {
        int subenc = ZlibHexTile.SUBENC_BACKGROUND_SPECIFIED
                | ZlibHexTile.SUBENC_FOREGROUND_SPECIFIED;
        ZlibHexTile msg = ZlibHexTile.newBuilder()
                .subencoding(subenc)
                .background(null)
                .foreground(null)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> msg.write(baos));
        assertEquals(1, baos.size()); // only subencoding byte
    }

    // -----------------------------------------------------------------------
    // GiiIo little-endian write/read paths
    // -----------------------------------------------------------------------

    /**
     * GiiDeviceDestruction with bigEndian=false exercises GiiIo.writeEU16(LE) and
     * GiiIo.writeEU32(LE) in write(), and GiiIo.readEU16(LE) and GiiIo.readEU32(LE)
     * in read().
     *
     * <p>RFB GII extension wire format for DeviceDestruction:
     * <pre>
     * message-type (1 byte = 253)
     * endian-and-sub-type (1 byte): low 2 bits = event sub-type, high bit = big-endian flag
     * length (EU16): 4
     * device-origin (EU32)
     * </pre>
     */
    @Test
    void testGiiDeviceDestruction_littleEndian_writeRead() throws IOException {
        GiiDeviceDestruction orig = GiiDeviceDestruction.newBuilder()
                .bigEndian(false)
                .deviceOrigin(0x12345678L)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        orig.write(baos);
        byte[] bytes = baos.toByteArray();
        assertEquals(8, bytes.length); // 1 type + 1 endian+sub + 2 len + 4 origin
        assertEquals((byte) 253, bytes[0]); // message-type
        // read() skips the first byte (message-type); pass bytes[1..]
        InputStream in = new ByteArrayInputStream(bytes, 1, bytes.length - 1);
        GiiDeviceDestruction copy = GiiDeviceDestruction.read(in);
        assertFalse(copy.bigEndian());
        assertEquals(0x12345678L, copy.deviceOrigin());
    }

    /**
     * GiiValuator with bigEndian=false exercises GiiIo.writeES32(LE) and writeEU32(LE)
     * in write(). Using a round-trip via write(false) + read(false).
     *
     * <p>GII Valuator record wire layout (per endianness):
     * <pre>
     * index      (EU32)
     * longName   (74 bytes + NUL)
     * shortName  (4 bytes + NUL)
     * range-min, range-center, range-max (ES32 each)
     * si-unit    (EU32)
     * si-add, si-mul, si-div, si-shift (ES32 each)
     * </pre>
     */
    @Test
    void testGiiValuator_littleEndian_writeRead() throws IOException {
        GiiValuator orig = GiiValuator.newBuilder()
                .index(1)
                .longName("X")
                .shortName("X")
                .rangeMin(-100)
                .rangeCenter(0)
                .rangeMax(100)
                .siUnit(0)
                .siAdd(0)
                .siMul(1)
                .siDiv(1)
                .siShift(0)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        orig.write(baos, false); // bigEndian=false → covers GiiIo LE write paths
        GiiValuator copy = GiiValuator.read(new ByteArrayInputStream(baos.toByteArray()), false);
        assertEquals(1L, copy.index());
        assertEquals("X", copy.longName());
        assertEquals(-100, copy.rangeMin());
        assertEquals(100, copy.rangeMax());
    }

    // -----------------------------------------------------------------------
    // VncSocketClient String-host constructor
    // -----------------------------------------------------------------------

    /**
     * VncSocketClient(String host, int port, VncClientFactory) is an alternative
     * to the InetSocketAddress constructor; exercises the same connection logic.
     *
     * <p>The RFB handshake is identical to other integration tests; this test
     * only verifies the constructor path competes without error.
     */
    @Test
    @Timeout(10)
    void testVncSocketClient_stringHostConstructor() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        VncSocketServer server = new VncSocketServer(0, handle -> new VncServer() {
            @Override
            public ServerInit onClientInit(ClientInit clientInit) {
                return ServerInit.newBuilder()
                        .framebufferWidth(1).framebufferHeight(1)
                        .pixelFormat(PF_32BPP).name("StringTest").build();
            }

            @Override
            public void onFramebufferUpdateRequest(FramebufferUpdateRequest msg) {
            }
        });
        int port = server.getLocalPort();
        server.start();

        try {
            VncSocketClient client = new VncSocketClient("127.0.0.1", port,
                    handle -> new VncClient() {
                        @Override
                        public void onServerInit(ServerInit si) {
                            latch.countDown();
                        }

                        @Override
                        public void onFramebufferUpdate(FramebufferUpdate msg) {
                        }
                    });
            client.start();
            assertTrue(latch.await(5, TimeUnit.SECONDS), "ServerInit not received via string constructor");
            client.close();
        } finally {
            server.close();
        }
    }

    // -----------------------------------------------------------------------
    // VncSocketClient: unknown server message type → UnsupportedOperationException
    // -----------------------------------------------------------------------

    /**
     * When the server sends an unknown message-type byte (e.g. 0xFF), the
     * {@code ServerMessageDispatch} throws {@link UnsupportedOperationException}.
     * VncSocketClient catches this in the message loop, breaks, and calls
     * {@link VncClient#onClose()}.
     *
     * <p>This test uses a raw server socket that completes the full RFB 3.8
     * handshake and then sends byte 0xFF as an unknown message type.
     *
     * <pre>
     * RFB 3.8 handshake from server:
     *   "RFB 003.008\n"                      (12 bytes)
     *   SecurityTypes: count=1, type=1        (2 bytes)
     *   SecurityResult: status=0              (4 bytes)
     *   ServerInit: w, h, pf (16), name-len, name
     * Then sends: byte 0xFF (unknown msg type)
     * </pre>
     */
    @Test
    @Timeout(10)
    void testVncSocketClient_unknownMessageType_callsOnClose() throws Exception {
        CountDownLatch closedLatch = new CountDownLatch(1);

        ServerSocket raw = new ServerSocket(0);
        int rawPort = raw.getLocalPort();

        Thread serverThread = new Thread(() -> {
            try (Socket s = raw.accept()) {
                java.io.DataOutputStream dos = new java.io.DataOutputStream(s.getOutputStream());
                java.io.DataInputStream dis = new java.io.DataInputStream(s.getInputStream());

                // Write server protocol version
                s.getOutputStream().write("RFB 003.008\n".getBytes());
                s.getOutputStream().flush();

                // Read client version (12 bytes)
                byte[] versionBuf = new byte[12];
                dis.readFully(versionBuf);

                // Write SecurityTypes: count=1, type=1 (None)
                dos.writeByte(1);  // count
                dos.writeByte(1);  // type None
                dos.flush();

                // Read security type selection (1 byte)
                dis.readUnsignedByte();

                // Write SecurityResult: 0 = OK
                dos.writeInt(0);
                dos.flush();

                // Read ClientInit (1 byte)
                dis.readUnsignedByte();

                // Write ServerInit: width=1, height=1, pf (16 bytes), name-len=4, name="Test"
                dos.writeShort(1);      // framebuffer width
                dos.writeShort(1);      // framebuffer height
                // PixelFormat (16 bytes): bpp=8,depth=8,bigEndian=0,trueColour=0,rMax=7,gMax=7,bMax=3,rShift=5,gShift=2,bShift=0,pad*3
                dos.writeByte(8);  dos.writeByte(8);  dos.writeByte(0); dos.writeByte(0);
                dos.writeShort(7); dos.writeShort(7); dos.writeShort(3);
                dos.writeByte(5);  dos.writeByte(2);  dos.writeByte(0);
                dos.writeByte(0);  dos.writeByte(0);  dos.writeByte(0);
                dos.writeInt(4);   // name length
                dos.write(new byte[]{'T', 'e', 's', 't'});
                dos.flush();

                // Now send an unknown message type byte
                Thread.sleep(200); // give client time to process ServerInit
                dos.writeByte(0xFF); // unknown
                dos.flush();

                Thread.sleep(2000); // give client time to close
            } catch (Exception ignored) {
            }
        }, "raw-server-unknown-msg");
        serverThread.setDaemon(true);
        serverThread.start();

        try {
            VncSocketClient client = new VncSocketClient(
                    new InetSocketAddress("127.0.0.1", rawPort),
                    handle -> new VncClient() {
                        @Override
                        public void onServerInit(ServerInit si) {
                        }

                        @Override
                        public void onFramebufferUpdate(FramebufferUpdate msg) {
                        }

                        @Override
                        public void onClose() {
                            closedLatch.countDown();
                        }
                    });
            client.start();

            assertTrue(closedLatch.await(5, TimeUnit.SECONDS),
                    "onClose not called after unknown message type");
            client.close();
        } finally {
            raw.close();
        }
    }

    // -----------------------------------------------------------------------
    // VncSocketServer: unknown client message type → UnsupportedOperationException
    // -----------------------------------------------------------------------

    /**
     * When the client sends an unknown message-type byte (e.g. 0xFF), the
     * {@code ClientMessageDispatch} throws {@link UnsupportedOperationException}.
     * VncSocketServer catches this in the message loop and calls
     * {@link VncServer#onClose()}.
     *
     * <p>The raw client below manually completes the full RFB 3.8 handshake
     * and then sends byte 0xFF as an unknown client message type.
     */
    @Test
    @Timeout(10)
    void testVncSocketServer_unknownClientMessageType_callsOnClose() throws Exception {
        CountDownLatch serverClosedLatch = new CountDownLatch(1);

        VncSocketServer server = new VncSocketServer(0, handle -> new VncServer() {
            @Override
            public ServerInit onClientInit(ClientInit clientInit) {
                return ServerInit.newBuilder()
                        .framebufferWidth(1).framebufferHeight(1)
                        .pixelFormat(PF_32BPP).name("USOETest").build();
            }

            @Override
            public void onFramebufferUpdateRequest(FramebufferUpdateRequest msg) {
            }

            @Override
            public void onClose() {
                serverClosedLatch.countDown();
            }
        });
        int port = server.getLocalPort();
        server.start();

        Thread clientThread = new Thread(() -> {
            try (Socket s = new Socket("127.0.0.1", port)) {
                java.io.DataInputStream dis = new java.io.DataInputStream(s.getInputStream());
                java.io.DataOutputStream dos = new java.io.DataOutputStream(s.getOutputStream());

                // Read server protocol version (12 bytes) and echo back
                byte[] version = new byte[12];
                dis.readFully(version);
                dos.write("RFB 003.008\n".getBytes());
                dos.flush();

                // Read SecurityTypes: count + types
                int count = dis.readUnsignedByte();
                for (int i = 0; i < count; i++) {
                    dis.readUnsignedByte();
                }

                // Write SecurityTypeSelection: type 1 (None)
                dos.writeByte(1);
                dos.flush();

                // Read SecurityResult (4 bytes)
                dis.readInt();

                // Write ClientInit: shared=1
                dos.writeByte(1);
                dos.flush();

                // Read ServerInit: width(2)+height(2)+pf(16)+namelen(4)+name
                int w = dis.readUnsignedShort();
                int h = dis.readUnsignedShort();
                byte[] pf = new byte[16];
                dis.readFully(pf);
                int nameLen = dis.readInt();
                byte[] name = new byte[nameLen];
                dis.readFully(name);

                // Send unknown message type
                Thread.sleep(200);
                dos.writeByte(0xFF); // unknown client message type
                dos.flush();

                Thread.sleep(2000);
            } catch (Exception ignored) {
            }
        }, "raw-client-unknown-msg");
        clientThread.setDaemon(true);
        clientThread.start();

        try {
            assertTrue(serverClosedLatch.await(5, TimeUnit.SECONDS),
                    "VncServer.onClose() not called after unknown client message type");
        } finally {
            server.close();
        }
    }
}
