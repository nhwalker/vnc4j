package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.messages.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying that server-to-client messages conform to the byte-level
 * format specified in the RFB protocol specification (rfbproto.rst.txt).
 */
class ServerMessageByteFormatTest {

    @FunctionalInterface
    interface Writable {
        void write(java.io.OutputStream out) throws IOException;
    }

    private byte[] serialize(Writable writable) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writable.write(baos);
        return baos.toByteArray();
    }

    // -----------------------------------------------------------------------
    // Bell
    // -----------------------------------------------------------------------

    /**
     * Verifies the Bell message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - Bell:
     *
     *   Ring a bell on the client if it has one.
     *
     *   =============== ==================== ========== =======================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== =======================
     *   1               U8                   2          message-type
     *   =============== ==================== ========== =======================
     * </pre>
     */
    @Test
    void testBell_byteFormat() throws IOException {
        Bell msg = Bell.newBuilder().build();
        byte[] bytes = serialize(msg::write);

        assertEquals(1, bytes.length, "Bell must be exactly 1 byte");
        assertEquals((byte) 2, bytes[0], "Bell message-type must be 2");
    }

    // -----------------------------------------------------------------------
    // ServerCutText
    // -----------------------------------------------------------------------

    /**
     * Verifies the ServerCutText message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - ServerCutText:
     *
     *   The server has new ISO 8859-1 (Latin-1) text in its cut buffer.
     *   Ends of lines are represented by the linefeed / newline character (value 10) alone.
     *
     *   =============== ==================== ========== =======================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== =======================
     *   1               U8                   3          message-type
     *   3                                               padding
     *   4               U32                             length
     *   length          U8 array                        text
     *   =============== ==================== ========== =======================
     * </pre>
     */
    @Test
    void testServerCutText_byteFormat() throws IOException {
        byte[] textBytes = "Clipboard".getBytes(StandardCharsets.ISO_8859_1);
        ServerCutText msg = ServerCutText.newBuilder()
                .text(textBytes)
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 1 + 3 + 4 + 9 = 17 bytes
        assertEquals(17, bytes.length);

        // byte 0: message-type = 3
        assertEquals((byte) 3, bytes[0], "message-type must be 3 for ServerCutText");
        // bytes 1-3: padding
        assertEquals((byte) 0, bytes[1]);
        assertEquals((byte) 0, bytes[2]);
        assertEquals((byte) 0, bytes[3]);
        // bytes 4-7: length = 9 (big-endian U32)
        assertEquals((byte) 0, bytes[4]);
        assertEquals((byte) 0, bytes[5]);
        assertEquals((byte) 0, bytes[6]);
        assertEquals((byte) 9, bytes[7]);
        // bytes 8-16: "Clipboard"
        byte[] actualText = new byte[9];
        System.arraycopy(bytes, 8, actualText, 0, 9);
        assertArrayEquals(textBytes, actualText);
    }

    /**
     * Verifies the ServerCutText message with empty text.
     */
    @Test
    void testServerCutText_empty_byteFormat() throws IOException {
        ServerCutText msg = ServerCutText.newBuilder()
                .text(new byte[0])
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 1 + 3 + 4 = 8 bytes
        assertEquals(8, bytes.length);
        assertEquals((byte) 3, bytes[0]);
        assertEquals((byte) 0, bytes[1]);
        assertEquals((byte) 0, bytes[2]);
        assertEquals((byte) 0, bytes[3]);
        assertEquals((byte) 0, bytes[4]);
        assertEquals((byte) 0, bytes[5]);
        assertEquals((byte) 0, bytes[6]);
        assertEquals((byte) 0, bytes[7]);
    }

    // -----------------------------------------------------------------------
    // SetColourMapEntries
    // -----------------------------------------------------------------------

    /**
     * Verifies the SetColourMapEntries message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - SetColourMapEntries:
     *
     *   When the pixel format uses a "colour map", this message tells the client
     *   that the specified pixel values should be mapped to the given RGB intensities.
     *
     *   =============== ==================== ========== =======================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== =======================
     *   1               U8                   1          message-type
     *   1                                               padding
     *   2               U16                             first-colour
     *   2               U16                             number-of-colours
     *   =============== ==================== ========== =======================
     *
     *   followed by number-of-colours repetitions of:
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   2               U16                             red
     *   2               U16                             green
     *   2               U16                             blue
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testSetColourMapEntries_byteFormat() throws IOException {
        ColourMapEntry red = ColourMapEntry.newBuilder()
                .red(0xFFFF).green(0x0000).blue(0x0000).build();
        ColourMapEntry green = ColourMapEntry.newBuilder()
                .red(0x0000).green(0xFFFF).blue(0x0000).build();
        SetColourMapEntries msg = SetColourMapEntries.newBuilder()
                .firstColour(10)
                .colours(List.of(red, green))
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 1 + 1 + 2 + 2 + 2*6 = 18 bytes
        assertEquals(18, bytes.length);

        // byte 0: message-type = 1
        assertEquals((byte) 1, bytes[0], "message-type must be 1 for SetColourMapEntries");
        // byte 1: padding
        assertEquals((byte) 0, bytes[1]);
        // bytes 2-3: first-colour = 10 (big-endian U16)
        assertEquals((byte) 0, bytes[2]);
        assertEquals((byte) 10, bytes[3]);
        // bytes 4-5: number-of-colours = 2 (big-endian U16)
        assertEquals((byte) 0, bytes[4]);
        assertEquals((byte) 2, bytes[5]);
        // Red entry: red=0xFFFF, green=0x0000, blue=0x0000
        assertEquals((byte) 0xFF, bytes[6]);
        assertEquals((byte) 0xFF, bytes[7]);
        assertEquals((byte) 0x00, bytes[8]);
        assertEquals((byte) 0x00, bytes[9]);
        assertEquals((byte) 0x00, bytes[10]);
        assertEquals((byte) 0x00, bytes[11]);
        // Green entry: red=0x0000, green=0xFFFF, blue=0x0000
        assertEquals((byte) 0x00, bytes[12]);
        assertEquals((byte) 0x00, bytes[13]);
        assertEquals((byte) 0xFF, bytes[14]);
        assertEquals((byte) 0xFF, bytes[15]);
        assertEquals((byte) 0x00, bytes[16]);
        assertEquals((byte) 0x00, bytes[17]);
    }

    /**
     * Verifies the SetColourMapEntries message with no colour entries.
     */
    @Test
    void testSetColourMapEntries_empty_byteFormat() throws IOException {
        SetColourMapEntries msg = SetColourMapEntries.newBuilder()
                .firstColour(0)
                .colours(List.of())
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 1 + 1 + 2 + 2 = 6 bytes
        assertEquals(6, bytes.length);
        assertEquals((byte) 1, bytes[0]);  // message-type
        assertEquals((byte) 0, bytes[1]);  // padding
        assertEquals((byte) 0, bytes[2]);  // first-colour high
        assertEquals((byte) 0, bytes[3]);  // first-colour low
        assertEquals((byte) 0, bytes[4]);  // count high
        assertEquals((byte) 0, bytes[5]);  // count low
    }

    /**
     * Verifies that each ColourMapEntry is exactly 6 bytes (3 x U16 values).
     *
     * <pre>
     * From rfbproto.rst.txt - SetColourMapEntries:
     *
     *   Each colour entry is 6 bytes: red (U16) + green (U16) + blue (U16).
     * </pre>
     */
    @Test
    void testColourMapEntry_size() throws IOException {
        ColourMapEntry entry = ColourMapEntry.newBuilder()
                .red(0x1234).green(0x5678).blue(0x9ABC).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        entry.write(baos);
        byte[] bytes = baos.toByteArray();

        assertEquals(6, bytes.length, "Each ColourMapEntry must be exactly 6 bytes");
        // red = 0x1234 (big-endian U16)
        assertEquals((byte) 0x12, bytes[0]);
        assertEquals((byte) 0x34, bytes[1]);
        // green = 0x5678
        assertEquals((byte) 0x56, bytes[2]);
        assertEquals((byte) 0x78, bytes[3]);
        // blue = 0x9ABC
        assertEquals((byte) 0x9A, bytes[4]);
        assertEquals((byte) 0xBC, bytes[5]);
    }

    // -----------------------------------------------------------------------
    // FramebufferUpdate
    // -----------------------------------------------------------------------

    /**
     * Verifies the FramebufferUpdate message header byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - FramebufferUpdate:
     *
     *   A framebuffer update consists of a sequence of rectangles of pixel data
     *   which the client should put into its framebuffer.
     *
     *   =============== ==================== ========== =======================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== =======================
     *   1               U8                   0          message-type
     *   1                                               padding
     *   2               U16                             number-of-rectangles
     *   =============== ==================== ========== =======================
     *
     *   This is followed by number-of-rectangles rectangles.
     *   Each rectangle header:
     *
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
    @Test
    void testFramebufferUpdate_empty_byteFormat() throws IOException {
        FramebufferUpdate msg = FramebufferUpdate.newBuilder()
                .rectangles(List.of())
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 1 + 1 + 2 = 4 bytes (header only, no rectangles)
        assertEquals(4, bytes.length, "FramebufferUpdate header must be 4 bytes");

        // byte 0: message-type = 0
        assertEquals((byte) 0, bytes[0], "message-type must be 0 for FramebufferUpdate");
        // byte 1: padding
        assertEquals((byte) 0, bytes[1]);
        // bytes 2-3: number-of-rectangles = 0
        assertEquals((byte) 0, bytes[2]);
        assertEquals((byte) 0, bytes[3]);
    }

    /**
     * Verifies the FramebufferUpdate message with a single Raw-encoded rectangle.
     *
     * <pre>
     * From rfbproto.rst.txt - FramebufferUpdate:
     *
     *   Each rectangle consists of a header followed by pixel data:
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   2               U16                             x-position
     *   2               U16                             y-position
     *   2               U16                             width
     *   2               U16                             height
     *   4               S32                             encoding-type (0 = Raw)
     *   =============== =============================== =======================
     *
     *   For Raw encoding, the pixel data is width * height * bytesPerPixel bytes.
     * </pre>
     */
    @Test
    void testFramebufferUpdate_withRawRectangle_byteFormat() throws IOException {
        // 1x1 pixel, 4 bytes per pixel (32bpp), black pixel
        byte[] pixelData = new byte[]{0x00, 0x00, 0x00, 0x00};
        RfbRectangleRaw rect = RfbRectangleRaw.newBuilder()
                .x(0).y(0).width(1).height(1)
                .pixels(pixelData)
                .build();
        FramebufferUpdate msg = FramebufferUpdate.newBuilder()
                .rectangles(List.of(rect))
                .build();
        byte[] bytes = serialize(msg::write);

        // Message header: 4 bytes
        // Rectangle header: 2+2+2+2+4 = 12 bytes
        // Pixel data: 4 bytes
        // Total: 20 bytes
        assertEquals(20, bytes.length);

        // message header
        assertEquals((byte) 0, bytes[0]);   // message-type = 0
        assertEquals((byte) 0, bytes[1]);   // padding
        assertEquals((byte) 0, bytes[2]);   // rect count high
        assertEquals((byte) 1, bytes[3]);   // rect count low = 1

        // rectangle header
        assertEquals((byte) 0, bytes[4]);   // x high
        assertEquals((byte) 0, bytes[5]);   // x low = 0
        assertEquals((byte) 0, bytes[6]);   // y high
        assertEquals((byte) 0, bytes[7]);   // y low = 0
        assertEquals((byte) 0, bytes[8]);   // width high
        assertEquals((byte) 1, bytes[9]);   // width low = 1
        assertEquals((byte) 0, bytes[10]);  // height high
        assertEquals((byte) 1, bytes[11]);  // height low = 1
        // encoding-type = 0 (Raw) as big-endian S32
        assertEquals((byte) 0, bytes[12]);
        assertEquals((byte) 0, bytes[13]);
        assertEquals((byte) 0, bytes[14]);
        assertEquals((byte) 0, bytes[15]);

        // pixel data
        assertEquals((byte) 0, bytes[16]);
        assertEquals((byte) 0, bytes[17]);
        assertEquals((byte) 0, bytes[18]);
        assertEquals((byte) 0, bytes[19]);
    }

    /**
     * Verifies that the number-of-rectangles field in FramebufferUpdate is a big-endian U16.
     */
    @Test
    void testFramebufferUpdate_rectangleCount_bigEndian() throws IOException {
        // Build a list of 256 raw rectangles (to verify big-endian encoding of count)
        byte[] emptyPixel = new byte[4];
        List<RfbRectangleRaw> rects = new java.util.ArrayList<>();
        for (int i = 0; i < 256; i++) {
            rects.add(RfbRectangleRaw.newBuilder()
                    .x(i).y(0).width(1).height(1)
                    .pixels(emptyPixel)
                    .build());
        }
        FramebufferUpdate msg = FramebufferUpdate.newBuilder()
                .rectangles(new java.util.ArrayList<>(rects))
                .build();
        byte[] bytes = serialize(msg::write);

        // number-of-rectangles = 256, big-endian U16: 0x01 0x00
        assertEquals((byte) 0x01, bytes[2], "High byte of count=256 must be 0x01");
        assertEquals((byte) 0x00, bytes[3], "Low byte of count=256 must be 0x00");
    }
}
