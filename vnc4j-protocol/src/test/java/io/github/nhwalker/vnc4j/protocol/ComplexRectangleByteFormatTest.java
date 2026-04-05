package io.github.nhwalker.vnc4j.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying that complex rectangle encodings (Raw, RRE, CoRRE, Hextile)
 * conform to the byte-level format specified in the RFB protocol specification
 * (rfbproto.rst.txt).
 */
class ComplexRectangleByteFormatTest {

    @FunctionalInterface
    interface Writable {
        void write(java.io.OutputStream out) throws IOException;
    }

    private byte[] serialize(Writable writable) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writable.write(baos);
        return baos.toByteArray();
    }

    private void assertRectHeader(byte[] bytes, int x, int y, int width, int height, int encodingType) {
        assertEquals((byte) ((x >> 8) & 0xFF), bytes[0]);
        assertEquals((byte) (x & 0xFF), bytes[1]);
        assertEquals((byte) ((y >> 8) & 0xFF), bytes[2]);
        assertEquals((byte) (y & 0xFF), bytes[3]);
        assertEquals((byte) ((width >> 8) & 0xFF), bytes[4]);
        assertEquals((byte) (width & 0xFF), bytes[5]);
        assertEquals((byte) ((height >> 8) & 0xFF), bytes[6]);
        assertEquals((byte) (height & 0xFF), bytes[7]);
        assertEquals((byte) ((encodingType >> 24) & 0xFF), bytes[8]);
        assertEquals((byte) ((encodingType >> 16) & 0xFF), bytes[9]);
        assertEquals((byte) ((encodingType >> 8) & 0xFF), bytes[10]);
        assertEquals((byte) (encodingType & 0xFF), bytes[11]);
    }

    // -----------------------------------------------------------------------
    // Raw
    // -----------------------------------------------------------------------

    /**
     * Verifies the Raw rectangle byte format (encoding type 0).
     *
     * <pre>
     * From rfbproto.rst.txt - Raw Encoding:
     *
     *   The simplest encoding type is raw pixel data. In this case the data
     *   consists of width * height pixel values (where bytesPerPixel depends
     *   on the pixel format). The first pixel is the top-left pixel.
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   width*height*   U8 array                        pixel data
     *   bytesPerPixel
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testRfbRectangleRaw_byteFormat() throws IOException {
        // 2x1 rectangle with 1-byte-per-pixel pixel data
        byte[] pixels = new byte[]{(byte) 0xAA, (byte) 0xBB};
        RfbRectangleRaw rect = RfbRectangleRaw.newBuilder()
                .x(0).y(0).width(2).height(1)
                .pixels(pixels)
                .build();
        byte[] bytes = serialize(rect::write);

        // 12 (header) + 2 (pixels) = 14 bytes
        assertEquals(14, bytes.length);
        assertRectHeader(bytes, 0, 0, 2, 1, RfbRectangleRaw.ENCODING_TYPE);
        assertEquals((byte) 0xAA, bytes[12]);
        assertEquals((byte) 0xBB, bytes[13]);
    }

    @Test
    void testRfbRectangleRaw_encodingType_is_0() {
        assertEquals(0, RfbRectangleRaw.ENCODING_TYPE);
    }

    // -----------------------------------------------------------------------
    // RreSubrect
    // -----------------------------------------------------------------------

    /**
     * Verifies the RreSubrect byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - RRE Encoding subrect:
     *
     *   Each subrect consists of:
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   bytesPerPixel   U8 array                        pixel value
     *   2               U16                             x-position
     *   2               U16                             y-position
     *   2               U16                             width
     *   2               U16                             height
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testRreSubrect_byteFormat() throws IOException {
        // 1-byte pixel value
        RreSubrect sr = RreSubrect.newBuilder()
                .pixel(new byte[]{(byte) 0xFF})
                .x(2).y(3).width(4).height(5)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        sr.write(baos);
        byte[] bytes = baos.toByteArray();

        // 1 (pixel) + 2 (x) + 2 (y) + 2 (w) + 2 (h) = 9 bytes
        assertEquals(9, bytes.length);
        assertEquals((byte) 0xFF, bytes[0], "pixel value");
        assertEquals((byte) 0, bytes[1]);
        assertEquals((byte) 2, bytes[2], "x = 2");
        assertEquals((byte) 0, bytes[3]);
        assertEquals((byte) 3, bytes[4], "y = 3");
        assertEquals((byte) 0, bytes[5]);
        assertEquals((byte) 4, bytes[6], "width = 4");
        assertEquals((byte) 0, bytes[7]);
        assertEquals((byte) 5, bytes[8], "height = 5");
    }

    // -----------------------------------------------------------------------
    // RRE Rectangle
    // -----------------------------------------------------------------------

    /**
     * Verifies the RRE rectangle byte format (encoding type 2).
     *
     * <pre>
     * From rfbproto.rst.txt - RRE Encoding:
     *
     *   RRE stands for Rise-and-Run-length Encoding and is essentially a
     *   two-dimensional analogue of run-length encoding (RLE).
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   4               U32                             number-of-subrects
     *   bytesPerPixel   U8 array                        background-pixel-value
     *   (repeated:)
     *   bytesPerPixel   U8 array                        subrect-pixel-value
     *   2               U16                             x-position
     *   2               U16                             y-position
     *   2               U16                             width
     *   2               U16                             height
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testRfbRectangleRre_byteFormat() throws IOException {
        byte[] background = new byte[]{0x00};  // 1 bpp
        RreSubrect sub = RreSubrect.newBuilder()
                .pixel(new byte[]{(byte) 0xFF})
                .x(1).y(1).width(2).height(2)
                .build();
        RfbRectangleRre rect = RfbRectangleRre.newBuilder()
                .x(0).y(0).width(10).height(10)
                .background(background)
                .subrects(List.of(sub))
                .build();
        byte[] bytes = serialize(rect::write);

        // 12 (header) + 4 (count) + 1 (bg pixel) + 9 (subrect) = 26 bytes
        assertEquals(26, bytes.length);
        assertRectHeader(bytes, 0, 0, 10, 10, RfbRectangleRre.ENCODING_TYPE);

        // number-of-subrects = 1 as big-endian U32
        assertEquals((byte) 0, bytes[12]);
        assertEquals((byte) 0, bytes[13]);
        assertEquals((byte) 0, bytes[14]);
        assertEquals((byte) 1, bytes[15]);
        // background pixel = 0x00
        assertEquals((byte) 0x00, bytes[16]);
        // subrect: pixel=0xFF
        assertEquals((byte) 0xFF, bytes[17]);
        // subrect x=1 (U16)
        assertEquals((byte) 0, bytes[18]);
        assertEquals((byte) 1, bytes[19]);
        // subrect y=1 (U16)
        assertEquals((byte) 0, bytes[20]);
        assertEquals((byte) 1, bytes[21]);
        // subrect width=2 (U16)
        assertEquals((byte) 0, bytes[22]);
        assertEquals((byte) 2, bytes[23]);
        // subrect height=2 (U16)
        assertEquals((byte) 0, bytes[24]);
        assertEquals((byte) 2, bytes[25]);
    }

    // -----------------------------------------------------------------------
    // CoRreSubrect
    // -----------------------------------------------------------------------

    /**
     * Verifies the CoRreSubrect byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - CoRRE Encoding subrect:
     *
     *   Each subrect is the same as RRE except coordinates and dimensions
     *   are U8 (1 byte each, range 0-255 since tiles are at most 255x255):
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   bytesPerPixel   U8 array                        pixel value
     *   1               U8                              x-position
     *   1               U8                              y-position
     *   1               U8                              width
     *   1               U8                              height
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testCoRreSubrect_byteFormat() throws IOException {
        CoRreSubrect sr = CoRreSubrect.newBuilder()
                .pixel(new byte[]{0x42})
                .x(10).y(20).width(5).height(6)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        sr.write(baos);
        byte[] bytes = baos.toByteArray();

        // 1 (pixel) + 1 (x) + 1 (y) + 1 (w) + 1 (h) = 5 bytes
        assertEquals(5, bytes.length);
        assertEquals((byte) 0x42, bytes[0], "pixel value");
        assertEquals((byte) 10, bytes[1], "x = 10");
        assertEquals((byte) 20, bytes[2], "y = 20");
        assertEquals((byte) 5, bytes[3], "width = 5");
        assertEquals((byte) 6, bytes[4], "height = 6");
    }

    // -----------------------------------------------------------------------
    // CoRRE Rectangle
    // -----------------------------------------------------------------------

    /**
     * Verifies the CoRRE rectangle byte format (encoding type 4).
     *
     * <pre>
     * From rfbproto.rst.txt - CoRRE Encoding:
     *
     *   CoRRE is a variant of RRE with U8 coordinates/dimensions to save
     *   bytes in the common case of small rectangles.
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   4               U32                             number-of-subrects
     *   bytesPerPixel   U8 array                        background-pixel-value
     *   (repeated:)
     *   bytesPerPixel   U8 array                        subrect-pixel-value
     *   1               U8                              x-position
     *   1               U8                              y-position
     *   1               U8                              width
     *   1               U8                              height
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testRfbRectangleCoRre_byteFormat() throws IOException {
        CoRreSubrect sub = CoRreSubrect.newBuilder()
                .pixel(new byte[]{(byte) 0xAB})
                .x(1).y(2).width(3).height(4)
                .build();
        RfbRectangleCoRre rect = RfbRectangleCoRre.newBuilder()
                .x(0).y(0).width(8).height(8)
                .background(new byte[]{0x00})
                .subrects(List.of(sub))
                .build();
        byte[] bytes = serialize(rect::write);

        // 12 (header) + 4 (count) + 1 (bg) + 5 (subrect) = 22 bytes
        assertEquals(22, bytes.length);
        assertRectHeader(bytes, 0, 0, 8, 8, RfbRectangleCoRre.ENCODING_TYPE);

        // count = 1 as big-endian U32
        assertEquals((byte) 0, bytes[12]);
        assertEquals((byte) 0, bytes[13]);
        assertEquals((byte) 0, bytes[14]);
        assertEquals((byte) 1, bytes[15]);
        // background = 0x00
        assertEquals((byte) 0x00, bytes[16]);
        // subrect pixel = 0xAB
        assertEquals((byte) 0xAB, bytes[17]);
        // subrect U8 coords
        assertEquals((byte) 1, bytes[18], "x = 1");
        assertEquals((byte) 2, bytes[19], "y = 2");
        assertEquals((byte) 3, bytes[20], "width = 3");
        assertEquals((byte) 4, bytes[21], "height = 4");
    }

    // -----------------------------------------------------------------------
    // HextileSubrect
    // -----------------------------------------------------------------------

    /**
     * Verifies the HextileSubrect packed byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - Hextile Encoding subrect:
     *
     *   Each subrect's position and size are packed into two bytes:
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   1               U8                              xy (high 4 bits = x, low 4 bits = y)
     *   1               U8                              wh (high 4 bits = w-1, low 4 bits = h-1)
     *   =============== =============================== =======================
     *
     *   (Optionally preceded by bytesPerPixel pixel value when SubrectsColoured)
     * </pre>
     */
    @Test
    void testHextileSubrect_packed_byteFormat() throws IOException {
        HextileSubrect sr = HextileSubrect.newBuilder()
                .x(3).y(5).width(4).height(2)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        sr.write(baos, false);  // not SubrectsColoured
        byte[] bytes = baos.toByteArray();

        // 2 bytes: xy and wh
        assertEquals(2, bytes.length);
        // xy: x=3 in high nibble, y=5 in low nibble = 0x35
        assertEquals((byte) 0x35, bytes[0], "xy = (3 << 4) | 5 = 0x35");
        // wh: (w-1)=3 in high nibble, (h-1)=1 in low nibble = 0x31
        assertEquals((byte) 0x31, bytes[1], "wh = (4-1) << 4 | (2-1) = 0x31");
    }

    @Test
    void testHextileSubrect_withColour_byteFormat() throws IOException {
        HextileSubrect sr = HextileSubrect.newBuilder()
                .pixel(new byte[]{(byte) 0xCC})
                .x(0).y(0).width(1).height(1)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        sr.write(baos, true);  // SubrectsColoured
        byte[] bytes = baos.toByteArray();

        // 1 (pixel) + 2 (xy + wh) = 3 bytes
        assertEquals(3, bytes.length);
        assertEquals((byte) 0xCC, bytes[0], "pixel value");
        assertEquals((byte) 0x00, bytes[1], "xy = 0x00 (x=0, y=0)");
        assertEquals((byte) 0x00, bytes[2], "wh = 0x00 (w-1=0, h-1=0)");
    }

    // -----------------------------------------------------------------------
    // HextileTile (Raw subencoding)
    // -----------------------------------------------------------------------

    /**
     * Verifies the HextileTile byte format with Raw subencoding.
     *
     * <pre>
     * From rfbproto.rst.txt - Hextile Encoding tile:
     *
     *   Each tile has a subencoding mask byte as first byte. If bit 0 (Raw) is
     *   set, the tile is followed by raw pixel data.
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   1               U8                              subencoding mask
     *   (if Raw):
     *   tileW*tileH*    U8 array                        raw pixel data
     *   bytesPerPixel
     *   =============== =============================== =======================
     *
     *   Subencoding mask bits:
     *     1 = Raw
     *     2 = BackgroundSpecified
     *     4 = ForegroundSpecified
     *     8 = AnySubrects
     *     16 = SubrectsColoured
     * </pre>
     */
    @Test
    void testHextileTile_rawSubencoding_byteFormat() throws IOException {
        // 2x2 tile with 1 byte per pixel
        byte[] rawPixels = new byte[]{0x01, 0x02, 0x03, 0x04};
        HextileTile tile = HextileTile.newBuilder()
                .subencoding(HextileTile.SUBENC_RAW)
                .rawPixels(rawPixels)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tile.write(baos);
        byte[] bytes = baos.toByteArray();

        // 1 (subencoding) + 4 (raw pixels) = 5 bytes
        assertEquals(5, bytes.length);
        assertEquals((byte) HextileTile.SUBENC_RAW, bytes[0], "subencoding = Raw (1)");
        assertEquals((byte) 0x01, bytes[1]);
        assertEquals((byte) 0x02, bytes[2]);
        assertEquals((byte) 0x03, bytes[3]);
        assertEquals((byte) 0x04, bytes[4]);
    }

    @Test
    void testHextileTile_subencodingConstants() {
        assertEquals(1, HextileTile.SUBENC_RAW, "Raw = 1");
        assertEquals(2, HextileTile.SUBENC_BACKGROUND_SPECIFIED, "BackgroundSpecified = 2");
        assertEquals(4, HextileTile.SUBENC_FOREGROUND_SPECIFIED, "ForegroundSpecified = 4");
        assertEquals(8, HextileTile.SUBENC_ANY_SUBRECTS, "AnySubrects = 8");
        assertEquals(16, HextileTile.SUBENC_SUBRECTS_COLOURED, "SubrectsColoured = 16");
    }

    // -----------------------------------------------------------------------
    // HextileTile (Background + Subrect subencoding)
    // -----------------------------------------------------------------------

    /**
     * Verifies HextileTile with BackgroundSpecified + AnySubrects subencoding.
     */
    @Test
    void testHextileTile_bgAndSubrects_byteFormat() throws IOException {
        int subenc = HextileTile.SUBENC_BACKGROUND_SPECIFIED | HextileTile.SUBENC_ANY_SUBRECTS;
        HextileSubrect sub = HextileSubrect.newBuilder()
                .x(1).y(2).width(3).height(1)
                .build();
        HextileTile tile = HextileTile.newBuilder()
                .subencoding(subenc)
                .background(new byte[]{0x00})
                .subrects(List.of(sub))
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tile.write(baos);
        byte[] bytes = baos.toByteArray();

        // 1 (subenc) + 1 (bg) + 1 (subrect count) + 2 (subrect xy+wh) = 5 bytes
        assertEquals(5, bytes.length);
        assertEquals((byte) subenc, bytes[0], "subencoding");
        assertEquals((byte) 0x00, bytes[1], "background pixel");
        assertEquals((byte) 1, bytes[2], "subrect count = 1");
        // xy: x=1 high nibble, y=2 low nibble = 0x12
        assertEquals((byte) 0x12, bytes[3], "xy = 0x12");
        // wh: (w-1)=2 high nibble, (h-1)=0 low nibble = 0x20
        assertEquals((byte) 0x20, bytes[4], "wh = 0x20");
    }

    // -----------------------------------------------------------------------
    // Hextile Rectangle
    // -----------------------------------------------------------------------

    /**
     * Verifies the Hextile rectangle byte format (encoding type 5).
     *
     * <pre>
     * From rfbproto.rst.txt - Hextile Encoding:
     *
     *   The rectangle is divided into 16x16 tiles (smaller at right/bottom
     *   edges). Each tile is encoded as a stream of tiles left-to-right,
     *   top-to-bottom.
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   (tiles, each):
     *   1               U8                              subencoding-mask
     *   (variable)      -                               tile data
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testRfbRectangleHextile_byteFormat() throws IOException {
        // Single raw tile
        HextileTile tile = HextileTile.newBuilder()
                .subencoding(HextileTile.SUBENC_RAW)
                .rawPixels(new byte[]{0x01, 0x02})
                .build();
        RfbRectangleHextile rect = RfbRectangleHextile.newBuilder()
                .x(0).y(0).width(2).height(1)
                .tiles(List.of(tile))
                .build();
        byte[] bytes = serialize(rect::write);

        // 12 (header) + 1 (subenc) + 2 (raw pixels) = 15 bytes
        assertEquals(15, bytes.length);
        assertRectHeader(bytes, 0, 0, 2, 1, RfbRectangleHextile.ENCODING_TYPE);
        assertEquals((byte) HextileTile.SUBENC_RAW, bytes[12]);
        assertEquals((byte) 0x01, bytes[13]);
        assertEquals((byte) 0x02, bytes[14]);
    }
}
