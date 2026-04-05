package io.github.nhwalker.vnc4j.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests targeting null-data write() branches and write/read round-trips
 * for classes that still have uncovered bytecodes:
 * - GiiDeviceCreationResponse/Creation with bigEndian=false (covers the LE endian branch)
 * - GiiDeviceCreation with null deviceName (covers the null name → empty bytes guard)
 * - RfbRectangleH264 with null data (write + toString)
 * - RfbRectangleTightPngJpeg/Png with null jpeg/png data
 * - toString() calls on instances with non-null fields (covers length paths)
 */
class NullDataAndWriteTest {

    private static final PixelFormat PF_32BPP = PixelFormat.newBuilder()
            .bitsPerPixel(32).depth(24).bigEndian(false).trueColour(true)
            .redMax(255).greenMax(255).blueMax(255)
            .redShift(16).greenShift(8).blueShift(0).build();

    // -----------------------------------------------------------------------
    // GiiDeviceCreationResponse: write with bigEndian=false
    // -----------------------------------------------------------------------

    /**
     * GiiDeviceCreationResponseImpl.write() writes:
     * {@code dos.writeByte(bigEndian ? 0x82 : 0x02)}.
     * The bigEndian=false branch (byte 0x02) is only covered when bigEndian=false.
     *
     * <p>Wire format: 1 msg-type + 1 endian+sub + EU16 length + EU32 origin = 8 bytes.
     */
    @Test
    void testGiiDeviceCreationResponse_littleEndian_writeRead() throws IOException {
        GiiDeviceCreationResponse orig = GiiDeviceCreationResponse.newBuilder()
                .bigEndian(false)
                .deviceOrigin(0xABCD1234L)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        orig.write(baos);
        byte[] bytes = baos.toByteArray();
        assertEquals(8, bytes.length);
        assertEquals((byte) 253, bytes[0]);   // message-type
        assertEquals((byte) 0x02, bytes[1]);  // little-endian sub-type

        // read() starts from byte[1] (message-type already consumed by dispatcher)
        InputStream in = new ByteArrayInputStream(bytes, 1, bytes.length - 1);
        GiiDeviceCreationResponse copy = GiiDeviceCreationResponse.read(in);
        assertFalse(copy.bigEndian());
        assertEquals(0xABCD1234L, copy.deviceOrigin());
    }

    // -----------------------------------------------------------------------
    // GiiDeviceCreation: write with bigEndian=false and null deviceName
    // -----------------------------------------------------------------------

    /**
     * GiiDeviceCreationImpl.write() has two null guards:
     * <ul>
     *   <li>{@code valuators != null ? valuators : List.of()} — unreachable after construction</li>
     *   <li>{@code deviceName != null ? deviceName.getBytes(...) : new byte[0]} — covered with null name</li>
     * </ul>
     * Also covers the {@code bigEndian=false} branch of the EU16/EU32 write calls.
     */
    @Test
    void testGiiDeviceCreation_littleEndian_write() throws IOException {
        GiiDeviceCreation orig = GiiDeviceCreation.newBuilder()
                .bigEndian(false)
                .deviceName("TestLE")
                .vendorId(0L).productId(0L).canGenerate(0L)
                .numRegisters(0L).numButtons(0L)
                .valuators(List.of())
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        orig.write(baos);
        assertTrue(baos.size() > 0);
        // Verify the endian byte is LE
        assertEquals((byte) 0x02, baos.toByteArray()[1]);
    }

    @Test
    void testGiiDeviceCreation_nullDeviceName_write() throws IOException {
        GiiDeviceCreation orig = GiiDeviceCreation.newBuilder()
                .bigEndian(true)
                .deviceName(null)   // null → empty bytes guard
                .vendorId(0L).productId(0L).canGenerate(0L)
                .numRegisters(0L).numButtons(0L)
                .valuators(List.of())
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> orig.write(baos));
        assertTrue(baos.size() > 0);
    }

    // -----------------------------------------------------------------------
    // RfbRectangleH264: null data in write() and toString()
    // -----------------------------------------------------------------------

    /**
     * RfbRectangleH264Impl.write() has {@code byte[] d = data != null ? data : new byte[0]}.
     * When data is null, the {@code new byte[0]} branch executes (2 instructions).
     *
     * <p>Also calls toString() on an instance with null data to cover the
     * {@code data != null ? data.length : "null"} else-branch.
     */
    @Test
    void testRfbRectangleH264_nullData_write() throws IOException {
        RfbRectangleH264 msg = RfbRectangleH264.newBuilder()
                .x(0).y(0).width(16).height(16).flags(0).data(null).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> msg.write(baos));
        // header(8) + enc(4) + len(4) + flags(4) = 20 bytes; no data
        assertEquals(20, baos.size());

        // toString() null data path: (data != null ? data.length : "null")
        String s = msg.toString();
        assertTrue(s.contains("null"), "toString() should indicate null data");
    }

    /**
     * toString() with non-null data covers the {@code data.length} branch.
     */
    @Test
    void testRfbRectangleH264_nonNullData_toString() {
        RfbRectangleH264 msg = RfbRectangleH264.newBuilder()
                .x(0).y(0).width(16).height(16).flags(0).data(new byte[]{1, 2}).build();
        String s = msg.toString();
        assertTrue(s.contains("2"), "toString() should show data length = 2");
    }

    // -----------------------------------------------------------------------
    // RfbRectangleTightPngJpeg: null jpegData in write()
    // -----------------------------------------------------------------------

    @Test
    void testRfbRectangleTightPngJpeg_nullJpegData_write() throws IOException {
        RfbRectangleTightPngJpeg msg = RfbRectangleTightPngJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0).jpegData(null).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> msg.write(baos));
        assertTrue(baos.size() >= 14); // header(8) + enc(4) + ctrl(1) + compact-len(1)
    }

    // -----------------------------------------------------------------------
    // RfbRectangleTightPngPng: null pngData in write()
    // -----------------------------------------------------------------------

    @Test
    void testRfbRectangleTightPngPng_nullPngData_write() throws IOException {
        RfbRectangleTightPngPng msg = RfbRectangleTightPngPng.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0).pngData(null).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> msg.write(baos));
        assertTrue(baos.size() >= 14);
    }

    // -----------------------------------------------------------------------
    // ZlibHexTile toString() - covers length paths for non-null fields
    // -----------------------------------------------------------------------

    /**
     * ZlibHexTileImpl.toString() has ternary expressions for rawPixels, zlibRawData,
     * and zlibSubrectData: {@code (x != null ? x.length : "null")}.
     * These are only covered when the respective fields are non-null AND toString() is called.
     */
    @Test
    void testZlibHexTile_toString_withNonNullFields() {
        ZlibHexTile tile = ZlibHexTile.newBuilder()
                .subencoding(ZlibHexTile.SUBENC_RAW)
                .rawPixels(new byte[]{0x11, 0x22})
                .build();
        String s = tile.toString();
        assertTrue(s.contains("2"), "toString() should show rawPixels.length = 2");
    }

    @Test
    void testZlibHexTile_toString_withZlibRawNonNull() {
        ZlibHexTile tile = ZlibHexTile.newBuilder()
                .subencoding(ZlibHexTile.SUBENC_ZLIB_RAW)
                .zlibRawData(new byte[]{0x01, 0x02, 0x03})
                .build();
        String s = tile.toString();
        assertTrue(s.contains("3"), "toString() should show zlibRawData.length = 3");
    }

    @Test
    void testZlibHexTile_toString_withZlibSubrectNonNull() {
        ZlibHexTile tile = ZlibHexTile.newBuilder()
                .subencoding(ZlibHexTile.SUBENC_ANY_SUBRECTS | ZlibHexTile.SUBENC_ZLIB)
                .zlibSubrectData(new byte[]{0x01, 0x02})
                .build();
        String s = tile.toString();
        assertTrue(s.contains("2"), "toString() should show zlibSubrectData.length = 2");
    }

    @Test
    void testZlibHexTile_toString_withNullFields() {
        ZlibHexTile tile = ZlibHexTile.newBuilder()
                .subencoding(0)
                .rawPixels(null)
                .zlibRawData(null)
                .zlibSubrectData(null)
                .build();
        String s = tile.toString();
        assertTrue(s.contains("null"), "toString() should show 'null' for null fields");
    }

    // -----------------------------------------------------------------------
    // RfbRectangleRaw toString() with null pixels
    // -----------------------------------------------------------------------

    @Test
    void testRfbRectangleRaw_toString_withNullPixels() {
        RfbRectangleRaw msg = RfbRectangleRaw.newBuilder()
                .x(0).y(0).width(1).height(1).pixels(null).build();
        String s = msg.toString();
        assertTrue(s.contains("null"));
    }

    @Test
    void testRfbRectangleRaw_toString_withNonNullPixels() {
        RfbRectangleRaw msg = RfbRectangleRaw.newBuilder()
                .x(0).y(0).width(1).height(1).pixels(new byte[]{0x42}).build();
        String s = msg.toString();
        assertTrue(s.contains("1")); // pixels.len=1
    }

    // -----------------------------------------------------------------------
    // RfbRectangleJpeg toString()
    // -----------------------------------------------------------------------

    @Test
    void testRfbRectangleJpeg_toString() {
        RfbRectangleJpeg msg = RfbRectangleJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).data(new byte[]{(byte)0xFF, (byte)0xD8}).build();
        String s = msg.toString();
        assertNotNull(s);
        assertTrue(s.contains("2")); // data.len=2
    }

    // -----------------------------------------------------------------------
    // RfbRectangleZlib / Zrle toString()
    // -----------------------------------------------------------------------

    @Test
    void testRfbRectangleZlib_toString_nullAndNonNull() {
        RfbRectangleZlib nullData = RfbRectangleZlib.newBuilder()
                .x(0).y(0).width(1).height(1).zlibData(null).build();
        assertTrue(nullData.toString().contains("null"));

        RfbRectangleZlib nonNullData = RfbRectangleZlib.newBuilder()
                .x(0).y(0).width(1).height(1).zlibData(new byte[]{1}).build();
        assertTrue(nonNullData.toString().contains("1"));
    }

    @Test
    void testRfbRectangleZrle_toString_nullAndNonNull() {
        RfbRectangleZrle nullData = RfbRectangleZrle.newBuilder()
                .x(0).y(0).width(1).height(1).zlibData(null).build();
        assertTrue(nullData.toString().contains("null"));

        RfbRectangleZrle nonNullData = RfbRectangleZrle.newBuilder()
                .x(0).y(0).width(1).height(1).zlibData(new byte[]{1, 2}).build();
        assertTrue(nonNullData.toString().contains("2"));
    }

    // -----------------------------------------------------------------------
    // RfbRectangleTightJpeg toString() with null jpegData
    // -----------------------------------------------------------------------

    @Test
    void testRfbRectangleTightJpeg_toString_nullAndNonNull() throws IOException {
        RfbRectangleTightJpeg nullData = RfbRectangleTightJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0).jpegData(null).build();
        String s1 = nullData.toString();
        assertNotNull(s1);

        RfbRectangleTightJpeg nonNull = RfbRectangleTightJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0)
                .jpegData(new byte[]{(byte)0xFF, (byte)0xD8}).build();
        String s2 = nonNull.toString();
        assertTrue(s2.contains("2"));
    }

    // -----------------------------------------------------------------------
    // RfbRectangleDispatch: remaining missed paths
    // -----------------------------------------------------------------------

    /**
     * Tests RfbRectangle.read() for encoding -239 (Cursor) via dispatch to ensure
     * the dispatch case is covered.
     */
    @Test
    void testRfbRectangleDispatch_cursorEncoding() throws IOException {
        // Build a 1x1 cursor rectangle (PF_32BPP: 4 bytes/pixel)
        RfbRectangleCursor orig = RfbRectangleCursor.newBuilder()
                .x(0).y(0).width(1).height(1)
                .pixels(new byte[]{0, 0, 0, (byte)0xFF})
                .bitmask(new byte[]{(byte)0x80}) // 1 byte bitmask for width=1
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        orig.write(baos);
        RfbRectangle result = RfbRectangle.read(
                new ByteArrayInputStream(baos.toByteArray()), PF_32BPP);
        assertInstanceOf(RfbRectangleCursor.class, result);
    }

    /**
     * Tests RfbRectangle.read() for encoding -314 (CursorWithAlpha) via dispatch.
     */
    @Test
    void testRfbRectangleDispatch_cursorWithAlphaEncoding() throws IOException {
        RfbRectangleCursorWithAlpha orig = RfbRectangleCursorWithAlpha.newBuilder()
                .x(0).y(0).width(0).height(0)
                .data(new byte[0]).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        orig.write(baos);
        RfbRectangle result = RfbRectangle.read(
                new ByteArrayInputStream(baos.toByteArray()), PF_32BPP);
        assertInstanceOf(RfbRectangleCursorWithAlpha.class, result);
    }
}
