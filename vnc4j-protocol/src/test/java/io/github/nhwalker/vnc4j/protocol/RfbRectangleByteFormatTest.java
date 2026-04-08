package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.messages.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying that RFB rectangle encodings conform to the byte-level format
 * specified in the RFB protocol specification (rfbproto.rst.txt).
 *
 * <p>Each rectangle consists of a 12-byte header followed by encoding-specific payload:
 * <pre>
 *   =============== =============================== =======================
 *   No. of bytes    Type                            Description
 *   =============== =============================== =======================
 *   2               U16                             x-position
 *   2               U16                             y-position
 *   2               U16                             width
 *   2               U16                             height
 *   4               S32                             encoding-type
 *   =============== =============================== =======================
 * </pre>
 */
class RfbRectangleByteFormatTest {

    @FunctionalInterface
    interface Writable {
        void write(java.io.OutputStream out) throws IOException;
    }

    private byte[] serialize(Writable writable) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writable.write(baos);
        return baos.toByteArray();
    }

    /**
     * Helper to verify the 12-byte rectangle header (x, y, w, h, encodingType).
     * The encoding-type is a big-endian S32 (4 bytes).
     */
    private void assertRectHeader(byte[] bytes, int x, int y, int width, int height, int encodingType) {
        // x: bytes 0-1
        assertEquals((byte) ((x >> 8) & 0xFF), bytes[0], "x high byte");
        assertEquals((byte) (x & 0xFF), bytes[1], "x low byte");
        // y: bytes 2-3
        assertEquals((byte) ((y >> 8) & 0xFF), bytes[2], "y high byte");
        assertEquals((byte) (y & 0xFF), bytes[3], "y low byte");
        // width: bytes 4-5
        assertEquals((byte) ((width >> 8) & 0xFF), bytes[4], "width high byte");
        assertEquals((byte) (width & 0xFF), bytes[5], "width low byte");
        // height: bytes 6-7
        assertEquals((byte) ((height >> 8) & 0xFF), bytes[6], "height high byte");
        assertEquals((byte) (height & 0xFF), bytes[7], "height low byte");
        // encoding-type: bytes 8-11 as big-endian S32
        assertEquals((byte) ((encodingType >> 24) & 0xFF), bytes[8], "encoding-type byte 0");
        assertEquals((byte) ((encodingType >> 16) & 0xFF), bytes[9], "encoding-type byte 1");
        assertEquals((byte) ((encodingType >> 8) & 0xFF), bytes[10], "encoding-type byte 2");
        assertEquals((byte) (encodingType & 0xFF), bytes[11], "encoding-type byte 3");
    }

    // -----------------------------------------------------------------------
    // CopyRect
    // -----------------------------------------------------------------------

    /**
     * Verifies the CopyRect rectangle byte format (encoding type 1).
     *
     * <pre>
     * From rfbproto.rst.txt - CopyRect Encoding:
     *
     *   The CopyRect encoding is the most efficient and simplest encoding type.
     *   It can be used when the client already has the same pixel data elsewhere
     *   in its framebuffer.
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   2               U16                             src-x-position
     *   2               U16                             src-y-position
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testRfbRectangleCopyRect_byteFormat() throws IOException {
        RfbRectangleCopyRect rect = RfbRectangleCopyRect.newBuilder()
                .x(10).y(20).width(100).height(50)
                .srcX(5).srcY(15)
                .build();
        byte[] bytes = serialize(rect::write);

        // 12-byte header + 4-byte payload (srcX + srcY) = 16 bytes
        assertEquals(16, bytes.length);
        assertRectHeader(bytes, 10, 20, 100, 50, RfbRectangleCopyRect.ENCODING_TYPE);

        // src-x = 5 as big-endian U16
        assertEquals((byte) 0, bytes[12]);
        assertEquals((byte) 5, bytes[13]);
        // src-y = 15 as big-endian U16
        assertEquals((byte) 0, bytes[14]);
        assertEquals((byte) 15, bytes[15]);
    }

    // -----------------------------------------------------------------------
    // DesktopSize
    // -----------------------------------------------------------------------

    /**
     * Verifies the DesktopSize pseudo-encoding rectangle byte format (encoding type -223).
     *
     * <pre>
     * From rfbproto.rst.txt - DesktopSize Pseudo-encoding:
     *
     *   The DesktopSize pseudo-encoding allows the server to tell the client
     *   that the framebuffer has been resized.
     *
     *   The rectangle has no payload; its width and height convey the new size.
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   (none)          -                               no payload
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testRfbRectangleDesktopSize_byteFormat() throws IOException {
        RfbRectangleDesktopSize rect = RfbRectangleDesktopSize.newBuilder()
                .x(0).y(0).width(1024).height(768)
                .build();
        byte[] bytes = serialize(rect::write);

        // Only header, no payload: 12 bytes
        assertEquals(12, bytes.length, "DesktopSize rectangle has no payload, only 12-byte header");
        assertRectHeader(bytes, 0, 0, 1024, 768, RfbRectangleDesktopSize.ENCODING_TYPE);
    }

    // -----------------------------------------------------------------------
    // LastRect
    // -----------------------------------------------------------------------

    /**
     * Verifies the LastRect pseudo-encoding rectangle byte format (encoding type -224).
     *
     * <pre>
     * From rfbproto.rst.txt - LastRect Pseudo-encoding:
     *
     *   When the server decides it has sent enough rectangles in the current
     *   FramebufferUpdate message, it can make the last one a LastRect encoding
     *   to indicate that there are no more.
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   (none)          -                               no payload
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testRfbRectangleLastRect_byteFormat() throws IOException {
        RfbRectangleLastRect rect = RfbRectangleLastRect.newBuilder()
                .x(0).y(0).width(0).height(0)
                .build();
        byte[] bytes = serialize(rect::write);

        // Only header, no payload: 12 bytes
        assertEquals(12, bytes.length, "LastRect rectangle has no payload, only 12-byte header");
        assertRectHeader(bytes, 0, 0, 0, 0, RfbRectangleLastRect.ENCODING_TYPE);
    }

    // -----------------------------------------------------------------------
    // Zlib
    // -----------------------------------------------------------------------

    /**
     * Verifies the Zlib rectangle byte format (encoding type 6).
     *
     * <pre>
     * From rfbproto.rst.txt - Zlib Encoding:
     *
     *   The Zlib encoding uses zlib to compress raw pixel data.
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   4               U32                             length
     *   length          U8 array                        zlib-data
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testRfbRectangleZlib_byteFormat() throws IOException {
        byte[] zlibData = new byte[]{0x78, (byte) 0x9C, 0x01, 0x02};
        RfbRectangleZlib rect = RfbRectangleZlib.newBuilder()
                .x(0).y(0).width(2).height(2)
                .zlibData(zlibData)
                .build();
        byte[] bytes = serialize(rect::write);

        // 12 (header) + 4 (length field) + 4 (zlib data) = 20 bytes
        assertEquals(20, bytes.length);
        assertRectHeader(bytes, 0, 0, 2, 2, RfbRectangleZlib.ENCODING_TYPE);

        // length = 4 as big-endian U32
        assertEquals((byte) 0, bytes[12]);
        assertEquals((byte) 0, bytes[13]);
        assertEquals((byte) 0, bytes[14]);
        assertEquals((byte) 4, bytes[15]);
        // zlib data
        assertEquals((byte) 0x78, bytes[16]);
        assertEquals((byte) 0x9C, bytes[17]);
        assertEquals((byte) 0x01, bytes[18]);
        assertEquals((byte) 0x02, bytes[19]);
    }

    // -----------------------------------------------------------------------
    // Zrle
    // -----------------------------------------------------------------------

    /**
     * Verifies the ZRLE rectangle byte format (encoding type 16).
     *
     * <pre>
     * From rfbproto.rst.txt - ZRLE Encoding:
     *
     *   ZRLE stands for Zlib Run-Length Encoding, and combines zlib compression,
     *   tiling, palettisation and run-length encoding.
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   4               U32                             length
     *   length          U8 array                        zrle-data (zlib compressed)
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testRfbRectangleZrle_byteFormat() throws IOException {
        byte[] zrleData = new byte[]{0x01, 0x02, 0x03};
        RfbRectangleZrle rect = RfbRectangleZrle.newBuilder()
                .x(5).y(10).width(16).height(16)
                .zlibData(zrleData)
                .build();
        byte[] bytes = serialize(rect::write);

        // 12 (header) + 4 (length field) + 3 (zrle data) = 19 bytes
        assertEquals(19, bytes.length);
        assertRectHeader(bytes, 5, 10, 16, 16, RfbRectangleZrle.ENCODING_TYPE);

        // length = 3 as big-endian U32
        assertEquals((byte) 0, bytes[12]);
        assertEquals((byte) 0, bytes[13]);
        assertEquals((byte) 0, bytes[14]);
        assertEquals((byte) 3, bytes[15]);
        assertEquals((byte) 0x01, bytes[16]);
        assertEquals((byte) 0x02, bytes[17]);
        assertEquals((byte) 0x03, bytes[18]);
    }

    // -----------------------------------------------------------------------
    // ExtendedDesktopSize
    // -----------------------------------------------------------------------

    /**
     * Verifies the ExtendedDesktopSize rectangle byte format (encoding type -308).
     *
     * <pre>
     * From rfbproto.rst.txt - ExtendedDesktopSize Pseudo-encoding:
     *
     *   This pseudo-encoding allows a server to tell the client about the
     *   server's layout of screens.
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   1               U8                              number-of-screens
     *   3                                               padding
     *   =============== =============================== =======================
     *
     *   followed by number-of-screens repetitions of:
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   4               U32                             id
     *   2               U16                             x-position
     *   2               U16                             y-position
     *   2               U16                             width
     *   2               U16                             height
     *   4               U32                             flags
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testRfbRectangleExtendedDesktopSize_byteFormat() throws IOException {
        Screen screen = Screen.newBuilder()
                .id(1).x(0).y(0).width(1920).height(1080).flags(0)
                .build();
        RfbRectangleExtendedDesktopSize rect = RfbRectangleExtendedDesktopSize.newBuilder()
                .x(0).y(0).width(1920).height(1080)
                .screens(List.of(screen))
                .build();
        byte[] bytes = serialize(rect::write);

        // 12 (header) + 1 (numScreens) + 3 (padding) + 16 (one screen) = 32 bytes
        assertEquals(32, bytes.length);
        assertRectHeader(bytes, 0, 0, 1920, 1080, RfbRectangleExtendedDesktopSize.ENCODING_TYPE);

        // number-of-screens = 1
        assertEquals((byte) 1, bytes[12]);
        // padding
        assertEquals((byte) 0, bytes[13]);
        assertEquals((byte) 0, bytes[14]);
        assertEquals((byte) 0, bytes[15]);

        // Screen: id=1 (U32 big-endian)
        assertEquals((byte) 0, bytes[16]);
        assertEquals((byte) 0, bytes[17]);
        assertEquals((byte) 0, bytes[18]);
        assertEquals((byte) 1, bytes[19]);
        // x=0 (U16)
        assertEquals((byte) 0, bytes[20]);
        assertEquals((byte) 0, bytes[21]);
        // y=0 (U16)
        assertEquals((byte) 0, bytes[22]);
        assertEquals((byte) 0, bytes[23]);
        // width=1920 (U16) = 0x0780
        assertEquals((byte) 0x07, bytes[24]);
        assertEquals((byte) 0x80, bytes[25]);
        // height=1080 (U16) = 0x0438
        assertEquals((byte) 0x04, bytes[26]);
        assertEquals((byte) 0x38, bytes[27]);
        // flags=0 (U32)
        assertEquals((byte) 0, bytes[28]);
        assertEquals((byte) 0, bytes[29]);
        assertEquals((byte) 0, bytes[30]);
        assertEquals((byte) 0, bytes[31]);
    }

    /**
     * Verifies ExtendedDesktopSize with no screens.
     */
    @Test
    void testRfbRectangleExtendedDesktopSize_noScreens_byteFormat() throws IOException {
        RfbRectangleExtendedDesktopSize rect = RfbRectangleExtendedDesktopSize.newBuilder()
                .x(0).y(0).width(800).height(600)
                .screens(List.of())
                .build();
        byte[] bytes = serialize(rect::write);

        // 12 (header) + 1 (numScreens) + 3 (padding) = 16 bytes
        assertEquals(16, bytes.length);
        assertRectHeader(bytes, 0, 0, 800, 600, RfbRectangleExtendedDesktopSize.ENCODING_TYPE);
        assertEquals((byte) 0, bytes[12], "number-of-screens = 0");
        assertEquals((byte) 0, bytes[13]);
        assertEquals((byte) 0, bytes[14]);
        assertEquals((byte) 0, bytes[15]);
    }

    // -----------------------------------------------------------------------
    // Cursor
    // -----------------------------------------------------------------------

    /**
     * Verifies the Cursor pseudo-encoding rectangle byte format (encoding type -239).
     *
     * <pre>
     * From rfbproto.rst.txt - Cursor Pseudo-encoding:
     *
     *   The Cursor pseudo-encoding allows the server to inform the client that
     *   it wishes the client to draw its own cursor locally.
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   w*h*bytesPerPixel   U8 array                   pixel data
     *   floor((w+7)/8)*h    U8 array                   bitmask
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testRfbRectangleCursor_byteFormat() throws IOException {
        // 2x2 cursor, 4 bytes per pixel (32bpp)
        byte[] pixels = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                                   0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10};
        // bitmask: floor((2+7)/8)*2 = 1*2 = 2 bytes
        byte[] bitmask = new byte[]{(byte) 0xC0, (byte) 0xC0};
        RfbRectangleCursor rect = RfbRectangleCursor.newBuilder()
                .x(1).y(1).width(2).height(2)
                .pixels(pixels).bitmask(bitmask)
                .build();
        byte[] bytes = serialize(rect::write);

        // 12 (header) + 16 (pixels) + 2 (bitmask) = 30 bytes
        assertEquals(30, bytes.length);
        assertRectHeader(bytes, 1, 1, 2, 2, RfbRectangleCursor.ENCODING_TYPE);

        // First pixel bytes
        assertEquals((byte) 0x01, bytes[12]);
        assertEquals((byte) 0x02, bytes[13]);
        // Bitmask starts at offset 28
        assertEquals((byte) 0xC0, bytes[28]);
        assertEquals((byte) 0xC0, bytes[29]);
    }

    // -----------------------------------------------------------------------
    // Encoding type constants
    // -----------------------------------------------------------------------

    /**
     * Verifies that the encoding type constants match their specified values from the spec.
     *
     * <pre>
     * From rfbproto.rst.txt - Encoding Types:
     *
     *   ==================== =================
     *   Encoding             Number
     *   ==================== =================
     *   Raw                  0
     *   CopyRect             1
     *   RRE                  2
     *   Hextile              5
     *   Zlib                 6
     *   Tight                7
     *   ZRLE                 16
     *   Cursor               -239 (0xFFFFFF11)
     *   DesktopSize          -223 (0xFFFFFF21)
     *   LastRect             -224 (0xFFFFFF20)
     *   ExtendedDesktopSize  -308 (0xFFFFFECC)
     *   ==================== =================
     * </pre>
     */
    @Test
    void testEncodingTypeConstants() {
        assertEquals(0, RfbRectangleRaw.ENCODING_TYPE);
        assertEquals(1, RfbRectangleCopyRect.ENCODING_TYPE);
        assertEquals(2, RfbRectangleRre.ENCODING_TYPE);
        assertEquals(5, RfbRectangleHextile.ENCODING_TYPE);
        assertEquals(6, RfbRectangleZlib.ENCODING_TYPE);
        assertEquals(7, RfbRectangleTight.ENCODING_TYPE);
        assertEquals(16, RfbRectangleZrle.ENCODING_TYPE);
        assertEquals(-239, RfbRectangleCursor.ENCODING_TYPE);
        assertEquals(-223, RfbRectangleDesktopSize.ENCODING_TYPE);
        assertEquals(-224, RfbRectangleLastRect.ENCODING_TYPE);
        assertEquals(-308, RfbRectangleExtendedDesktopSize.ENCODING_TYPE);
    }

    // -----------------------------------------------------------------------
    // Encoding type as big-endian S32 in header
    // -----------------------------------------------------------------------

    /**
     * Verifies that a negative encoding-type is written as a big-endian S32 in the header.
     * DesktopSize = -223 = 0xFFFFFF21 in two's complement.
     */
    @Test
    void testNegativeEncodingType_bigEndianS32() throws IOException {
        RfbRectangleDesktopSize rect = RfbRectangleDesktopSize.newBuilder()
                .x(0).y(0).width(800).height(600)
                .build();
        byte[] bytes = serialize(rect::write);

        // encoding-type = -223 = 0xFFFFFF21
        assertEquals((byte) 0xFF, bytes[8]);
        assertEquals((byte) 0xFF, bytes[9]);
        assertEquals((byte) 0xFF, bytes[10]);
        assertEquals((byte) 0x21, bytes[11]);
    }
}
