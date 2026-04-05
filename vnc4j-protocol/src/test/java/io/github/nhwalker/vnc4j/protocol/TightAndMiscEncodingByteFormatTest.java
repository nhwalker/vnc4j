package io.github.nhwalker.vnc4j.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying Tight, JPEG, H.264, and ZlibHex rectangle encoding byte formats
 * against the RFB protocol specification (rfbproto.rst.txt).
 */
class TightAndMiscEncodingByteFormatTest {

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
    // Tight – Fill compression (ctrl nibble = 0x8_)
    // -----------------------------------------------------------------------

    /**
     * Verifies the TightFill rectangle byte format (Tight encoding, FillCompression).
     *
     * <pre>
     * From rfbproto.rst.txt - Tight Encoding:
     *
     *   The first byte after the encoding-type field is a compression-control byte:
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   1               U8                              compression-control
     *   =============== =============================== =======================
     *
     *   For FillCompression (high nibble = 0x8), the byte is:
     *     bits 7-4: 0x8 (FillCompression flag)
     *     bits 3-0: stream reset flags
     *
     *   Followed by:
     *   =============== =============================== =======================
     *   tpixelSize      U8 array                        fill-colour (TPIXEL)
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testRfbRectangleTightFill_byteFormat() throws IOException {
        // 3-byte fill colour (typical TPIXEL for 32bpp/24depth)
        byte[] fillColor = new byte[]{(byte) 0xFF, 0x00, 0x00};  // red
        RfbRectangleTightFill rect = RfbRectangleTightFill.newBuilder()
                .x(0).y(0).width(10).height(10)
                .streamResets(0)
                .fillColor(fillColor)
                .build();
        byte[] bytes = serialize(rect::write);

        // 12 (header) + 1 (ctrl) + 3 (fill colour) = 16 bytes
        assertEquals(16, bytes.length);
        assertRectHeader(bytes, 0, 0, 10, 10, RfbRectangleTightFill.ENCODING_TYPE);

        // compression-control: FillCompression = 0x80 | streamResets
        assertEquals((byte) 0x80, bytes[12], "ctrl = FillCompression(0x80) | streamResets(0)");
        assertEquals((byte) 0xFF, bytes[13], "fill-colour R");
        assertEquals((byte) 0x00, bytes[14], "fill-colour G");
        assertEquals((byte) 0x00, bytes[15], "fill-colour B");
    }

    // -----------------------------------------------------------------------
    // Tight – JPEG compression (ctrl nibble = 0x9_)
    // -----------------------------------------------------------------------

    /**
     * Verifies the TightJpeg rectangle byte format (Tight encoding, JpegCompression).
     *
     * <pre>
     * From rfbproto.rst.txt - Tight Encoding JPEG:
     *
     *   For JpegCompression (high nibble = 0x9):
     *     compression-control byte: bits 7-4 = 0x9, bits 3-0 = stream reset flags
     *
     *   Followed by:
     *   =============== =============================== =======================
     *   1-3             compact-length                  length of JPEG data
     *   length          U8 array                        JPEG-compressed data
     *   =============== =============================== =======================
     *
     *   The compact-length encoding uses 1, 2, or 3 bytes:
     *     - 1 byte if length < 128
     *     - 2 bytes if 128 <= length < 16384
     *     - 3 bytes if length >= 16384
     * </pre>
     */
    @Test
    void testRfbRectangleTightJpeg_byteFormat() throws IOException {
        // Small JPEG data (< 128 bytes = 1-byte compact length)
        byte[] jpegData = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xD9};
        RfbRectangleTightJpeg rect = RfbRectangleTightJpeg.newBuilder()
                .x(0).y(0).width(4).height(4)
                .streamResets(0)
                .jpegData(jpegData)
                .build();
        byte[] bytes = serialize(rect::write);

        // 12 (header) + 1 (ctrl) + 1 (compact-len) + 4 (jpeg) = 18 bytes
        assertEquals(18, bytes.length);
        assertRectHeader(bytes, 0, 0, 4, 4, RfbRectangleTightJpeg.ENCODING_TYPE);

        // compression-control: JpegCompression = 0x90 | streamResets
        assertEquals((byte) 0x90, bytes[12], "ctrl = JpegCompression(0x90) | streamResets(0)");
        // compact-length = 4 (single byte, < 128)
        assertEquals((byte) 4, bytes[13], "compact-length = 4");
        // JPEG SOI marker
        assertEquals((byte) 0xFF, bytes[14]);
        assertEquals((byte) 0xD8, bytes[15]);
        assertEquals((byte) 0xFF, bytes[16]);
        assertEquals((byte) 0xD9, bytes[17]);
    }

    // -----------------------------------------------------------------------
    // Tight – Basic compression (ctrl nibble = 0x0_-0x7_)
    // -----------------------------------------------------------------------

    /**
     * Verifies the TightBasic rectangle byte format with CopyFilter (no filter byte).
     *
     * <pre>
     * From rfbproto.rst.txt - Tight Encoding Basic:
     *
     *   For BasicCompression (bit 7 = 0):
     *     compression-control byte layout:
     *       bits 7:   0 = BasicCompression
     *       bits 6:   ReadFilter flag (if set, filter byte follows ctrl)
     *       bits 5-4: stream number (0-3)
     *       bits 3-0: stream reset flags
     *
     *   If ReadFilter is set, a filter type byte follows:
     *     0 = CopyFilter (no additional data)
     *     1 = PaletteFilter (palette data follows)
     *     2 = GradientFilter (no additional data)
     *
     *   Followed by:
     *   =============== =============================== =======================
     *   1-3             compact-length                  length of compressed data
     *   length          U8 array                        zlib-compressed pixel data
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testRfbRectangleTightBasic_copyFilter_byteFormat() throws IOException {
        byte[] compressedData = new byte[]{0x01, 0x02, 0x03};
        RfbRectangleTightBasic rect = RfbRectangleTightBasic.newBuilder()
                .x(0).y(0).width(2).height(2)
                .streamResets(0)
                .streamNumber(0)
                .filterType(RfbRectangleTightBasic.FILTER_COPY)
                .compressedData(compressedData)
                .build();
        byte[] bytes = serialize(rect::write);

        // 12 (header) + 1 (ctrl, no ReadFilter) + 1 (compact-len) + 3 (data) = 17 bytes
        assertEquals(17, bytes.length);
        assertRectHeader(bytes, 0, 0, 2, 2, RfbRectangleTightBasic.ENCODING_TYPE);

        // ctrl: ReadFilter=0, streamNum=0, streamResets=0 → 0x00
        assertEquals((byte) 0x00, bytes[12], "ctrl = 0x00 (CopyFilter, stream 0, no resets)");
        // compact-length = 3
        assertEquals((byte) 3, bytes[13]);
        assertEquals((byte) 0x01, bytes[14]);
        assertEquals((byte) 0x02, bytes[15]);
        assertEquals((byte) 0x03, bytes[16]);
    }

    @Test
    void testRfbRectangleTightBasic_filterConstants() {
        assertEquals(0, RfbRectangleTightBasic.FILTER_COPY);
        assertEquals(1, RfbRectangleTightBasic.FILTER_PALETTE);
        assertEquals(2, RfbRectangleTightBasic.FILTER_GRADIENT);
    }

    // -----------------------------------------------------------------------
    // Tight compact-length encoding
    // -----------------------------------------------------------------------

    /**
     * Verifies the Tight compact-length 2-byte format for lengths 128-16383.
     *
     * <pre>
     * From rfbproto.rst.txt - Tight Compact Length:
     *
     *   The compact representation uses between 1 and 3 bytes:
     *     If value < 128:    1 byte  (high bit = 0)
     *     If value < 16384:  2 bytes (first byte bit7=1, second byte bit7=0)
     *     Otherwise:         3 bytes (first two bytes bit7=1, third byte is remainder)
     * </pre>
     */
    @Test
    void testRfbRectangleTightJpeg_twoByteCompactLength() throws IOException {
        // 200-byte JPEG data → compact-length uses 2 bytes (200 < 16384)
        byte[] jpegData = new byte[200];
        RfbRectangleTightJpeg rect = RfbRectangleTightJpeg.newBuilder()
                .x(0).y(0).width(1).height(1)
                .streamResets(0).jpegData(jpegData).build();
        byte[] bytes = serialize(rect::write);

        // 12 (header) + 1 (ctrl) + 2 (compact-len) + 200 (jpeg) = 215 bytes
        assertEquals(215, bytes.length);

        // compact-length = 200 in 2-byte format:
        // byte0: (200 & 0x7F) | 0x80 = 0x48 | 0x80 = 0xC8
        // byte1: (200 >> 7) & 0x7F = 1
        assertEquals((byte) 0xC8, bytes[13], "compact-length byte 0");
        assertEquals((byte) 0x01, bytes[14], "compact-length byte 1");
    }

    // -----------------------------------------------------------------------
    // JPEG encoding (encoding type 21)
    // -----------------------------------------------------------------------

    /**
     * Verifies the JPEG rectangle byte format (encoding type 21).
     *
     * <pre>
     * From rfbproto.rst.txt - JPEG Encoding:
     *
     *   The JPEG encoding sends raw JPEG-compressed data with no length prefix.
     *   The entire rectangle payload is a JPEG image.
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   (variable)      U8 array                        JPEG image data
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testRfbRectangleJpeg_byteFormat() throws IOException {
        byte[] jpegData = new byte[]{(byte) 0xFF, (byte) 0xD8, 0x01, 0x02, (byte) 0xFF, (byte) 0xD9};
        RfbRectangleJpeg rect = RfbRectangleJpeg.newBuilder()
                .x(5).y(10).width(100).height(50)
                .data(jpegData)
                .build();
        byte[] bytes = serialize(rect::write);

        // 12 (header) + 6 (jpeg data) = 18 bytes
        assertEquals(18, bytes.length);
        assertRectHeader(bytes, 5, 10, 100, 50, RfbRectangleJpeg.ENCODING_TYPE);

        // JPEG SOI
        assertEquals((byte) 0xFF, bytes[12]);
        assertEquals((byte) 0xD8, bytes[13]);
        // JPEG EOI
        assertEquals((byte) 0xFF, bytes[16]);
        assertEquals((byte) 0xD9, bytes[17]);
    }

    // -----------------------------------------------------------------------
    // H.264 encoding (encoding type 50)
    // -----------------------------------------------------------------------

    /**
     * Verifies the H.264 rectangle byte format (encoding type 50).
     *
     * <pre>
     * From rfbproto.rst.txt - H.264 Encoding:
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   4               U32                             length of H.264 data
     *   4               U32                             flags
     *   length          U8 array                        H.264 data
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testRfbRectangleH264_byteFormat() throws IOException {
        byte[] h264Data = new byte[]{0x00, 0x00, 0x00, 0x01, 0x67}; // SPS NAL unit
        RfbRectangleH264 rect = RfbRectangleH264.newBuilder()
                .x(0).y(0).width(1920).height(1080)
                .flags(1)
                .data(h264Data)
                .build();
        byte[] bytes = serialize(rect::write);

        // 12 (header) + 4 (length) + 4 (flags) + 5 (h264 data) = 25 bytes
        assertEquals(25, bytes.length);
        assertRectHeader(bytes, 0, 0, 1920, 1080, RfbRectangleH264.ENCODING_TYPE);

        // length = 5 as big-endian U32
        assertEquals((byte) 0, bytes[12]);
        assertEquals((byte) 0, bytes[13]);
        assertEquals((byte) 0, bytes[14]);
        assertEquals((byte) 5, bytes[15]);
        // flags = 1 as big-endian U32
        assertEquals((byte) 0, bytes[16]);
        assertEquals((byte) 0, bytes[17]);
        assertEquals((byte) 0, bytes[18]);
        assertEquals((byte) 1, bytes[19]);
        // H.264 data starts at offset 20
        assertEquals((byte) 0x00, bytes[20]);
        assertEquals((byte) 0x00, bytes[21]);
        assertEquals((byte) 0x00, bytes[22]);
        assertEquals((byte) 0x01, bytes[23]);
        assertEquals((byte) 0x67, bytes[24]);
    }

    // -----------------------------------------------------------------------
    // ZlibHex encoding (encoding type 8)
    // -----------------------------------------------------------------------

    /**
     * Verifies the ZlibHex rectangle byte format (encoding type 8).
     *
     * <pre>
     * From rfbproto.rst.txt - ZlibHex Encoding:
     *
     *   ZlibHex is a variant of Hextile encoding where the raw and subrect
     *   data can optionally be zlib-compressed.
     *
     *   The rectangle is divided into 16x16 tiles. Each tile has a subencoding
     *   byte with the same basic structure as Hextile, plus extra bits for zlib.
     *
     *   Additional subencoding bits (beyond Hextile's 5):
     *     32 (0x20) = ZlibRaw: raw tile data is zlib-compressed
     *     64 (0x40) = Zlib:    subrect data is zlib-compressed
     * </pre>
     */
    @Test
    void testRfbRectangleZlibHex_rawTile_byteFormat() throws IOException {
        // ZlibHex with a simple raw (uncompressed) tile
        ZlibHexTile tile = ZlibHexTile.newBuilder()
                .subencoding(ZlibHexTile.SUBENC_RAW)
                .rawPixels(new byte[]{0x01, 0x02})
                .build();
        RfbRectangleZlibHex rect = RfbRectangleZlibHex.newBuilder()
                .x(0).y(0).width(2).height(1)
                .tiles(List.of(tile))
                .build();
        byte[] bytes = serialize(rect::write);

        // 12 (header) + 1 (subencoding) + 2 (raw pixels) = 15 bytes
        assertEquals(15, bytes.length);
        assertRectHeader(bytes, 0, 0, 2, 1, RfbRectangleZlibHex.ENCODING_TYPE);

        assertEquals((byte) ZlibHexTile.SUBENC_RAW, bytes[12], "subencoding = Raw");
        assertEquals((byte) 0x01, bytes[13]);
        assertEquals((byte) 0x02, bytes[14]);
    }

    @Test
    void testZlibHexTile_subencodingConstants() {
        // ZlibHex adds two extra bits to Hextile subencoding
        assertEquals(1, ZlibHexTile.SUBENC_RAW);
        assertEquals(2, ZlibHexTile.SUBENC_BACKGROUND_SPECIFIED);
        assertEquals(4, ZlibHexTile.SUBENC_FOREGROUND_SPECIFIED);
        assertEquals(8, ZlibHexTile.SUBENC_ANY_SUBRECTS);
        assertEquals(16, ZlibHexTile.SUBENC_SUBRECTS_COLOURED);
        assertEquals(32, ZlibHexTile.SUBENC_ZLIB_RAW, "ZlibRaw = 32");
        assertEquals(64, ZlibHexTile.SUBENC_ZLIB, "Zlib = 64");
    }

    // -----------------------------------------------------------------------
    // TightPng variants
    // -----------------------------------------------------------------------

    /**
     * Verifies TightPngFill, TightPngJpeg, and TightPngPng encoding types are registered.
     */
    @Test
    void testTightPng_encodingTypeConstants() {
        assertEquals(-260, RfbRectangleTightPng.ENCODING_TYPE, "TightPng = -260");
    }

    @Test
    void testRfbRectangleTightPngFill_byteFormat() throws IOException {
        RfbRectangleTightPngFill rect = RfbRectangleTightPngFill.newBuilder()
                .x(0).y(0).width(2).height(2)
                .streamResets(0)
                .fillColor(new byte[]{0x11, 0x22, 0x33})
                .build();
        byte[] bytes = serialize(rect::write);

        // 12 (header) + 1 (ctrl) + 3 (fill colour) = 16 bytes
        assertEquals(16, bytes.length);
        assertRectHeader(bytes, 0, 0, 2, 2, RfbRectangleTightPngFill.ENCODING_TYPE);
        assertEquals((byte) 0x80, bytes[12], "ctrl = FillCompression (0x80)");
        assertEquals((byte) 0x11, bytes[13]);
        assertEquals((byte) 0x22, bytes[14]);
        assertEquals((byte) 0x33, bytes[15]);
    }

    @Test
    void testRfbRectangleTightPngJpeg_byteFormat() throws IOException {
        byte[] jpegData = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xD9};
        RfbRectangleTightPngJpeg rect = RfbRectangleTightPngJpeg.newBuilder()
                .x(0).y(0).width(4).height(4)
                .streamResets(0).jpegData(jpegData).build();
        byte[] bytes = serialize(rect::write);

        // 12 (header) + 1 (ctrl) + 1 (compact-len) + 4 (jpeg) = 18 bytes
        assertEquals(18, bytes.length);
        assertRectHeader(bytes, 0, 0, 4, 4, RfbRectangleTightPngJpeg.ENCODING_TYPE);
        assertEquals((byte) 0x90, bytes[12], "ctrl = JpegCompression (0x90)");
        assertEquals((byte) 4, bytes[13]);
    }

    @Test
    void testRfbRectangleTightPngPng_byteFormat() throws IOException {
        byte[] pngData = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}; // PNG magic
        RfbRectangleTightPngPng rect = RfbRectangleTightPngPng.newBuilder()
                .x(0).y(0).width(4).height(4)
                .streamResets(0).pngData(pngData).build();
        byte[] bytes = serialize(rect::write);

        // 12 (header) + 1 (ctrl) + 1 (compact-len) + 4 (png) = 18 bytes
        assertEquals(18, bytes.length);
        assertRectHeader(bytes, 0, 0, 4, 4, RfbRectangleTightPngPng.ENCODING_TYPE);
        assertEquals((byte) 0xA0, bytes[12], "ctrl = PngCompression (0xA0) for TightPng PngCompression");
        assertEquals((byte) 4, bytes[13]);
        assertEquals((byte) 0x89, bytes[14]);
    }
}
