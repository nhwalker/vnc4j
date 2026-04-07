package io.github.nhwalker.vnc4j.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests targeting null-data write() branches and write/read round-trips
 * for classes that still have uncovered bytecodes:
 * - RfbRectangleTightPngJpeg/Png with null jpeg/png data
 * - toString() calls on instances with non-null fields (covers length paths)
 */
class NullDataAndWriteTest {

    private static final PixelFormat PF_32BPP = PixelFormat.newBuilder()
            .bitsPerPixel(32).depth(24).bigEndian(false).trueColour(true)
            .redMax(255).greenMax(255).blueMax(255)
            .redShift(16).greenShift(8).blueShift(0).build();

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

}
