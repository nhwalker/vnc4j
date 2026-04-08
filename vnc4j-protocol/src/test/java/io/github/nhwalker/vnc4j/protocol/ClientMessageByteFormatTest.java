package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.messages.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying that client-to-server messages conform to the byte-level
 * format specified in the RFB protocol specification (rfbproto.rst.txt).
 */
class ClientMessageByteFormatTest {

    @FunctionalInterface
    interface Writable {
        void write(java.io.OutputStream out) throws IOException;
    }

    private byte[] serialize(Writable writable) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writable.write(baos);
        return baos.toByteArray();
    }

    private PixelFormat buildTrueColour32bpp() {
        return PixelFormat.newBuilder()
                .bitsPerPixel(32).depth(24)
                .bigEndian(false).trueColour(true)
                .redMax(255).greenMax(255).blueMax(255)
                .redShift(16).greenShift(8).blueShift(0)
                .build();
    }

    // -----------------------------------------------------------------------
    // SetPixelFormat
    // -----------------------------------------------------------------------

    /**
     * Verifies the SetPixelFormat message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - SetPixelFormat:
     *
     *   Sets the format in which pixel values should be sent in FramebufferUpdate messages.
     *
     *   =============== ==================== ========== =======================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== =======================
     *   1               U8                   0          message-type
     *   3                                               padding
     *   16              PIXEL_FORMAT                    pixel-format
     *   =============== ==================== ========== =======================
     *
     *   where PIXEL_FORMAT is:
     *
     *   =============== =================== ===================================
     *   No. of bytes    Type                Description
     *   =============== =================== ===================================
     *   1               U8                  bits-per-pixel
     *   1               U8                  depth
     *   1               U8                  big-endian-flag
     *   1               U8                  true-colour-flag
     *   2               U16                 red-max
     *   2               U16                 green-max
     *   2               U16                 blue-max
     *   1               U8                  red-shift
     *   1               U8                  green-shift
     *   1               U8                  blue-shift
     *   3                                   padding
     *   =============== =================== ===================================
     * </pre>
     */
    @Test
    void testSetPixelFormat_byteFormat() throws IOException {
        SetPixelFormat msg = SetPixelFormat.newBuilder()
                .pixelFormat(buildTrueColour32bpp())
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 1 + 3 + 16 = 20 bytes
        assertEquals(20, bytes.length, "SetPixelFormat must be exactly 20 bytes");

        // byte 0: message-type = 0
        assertEquals((byte) 0, bytes[0], "message-type must be 0 for SetPixelFormat");
        // bytes 1-3: padding
        assertEquals((byte) 0, bytes[1]);
        assertEquals((byte) 0, bytes[2]);
        assertEquals((byte) 0, bytes[3]);
        // bytes 4-19: pixel format
        assertEquals((byte) 32, bytes[4], "bits-per-pixel");
        assertEquals((byte) 24, bytes[5], "depth");
        assertEquals((byte) 0, bytes[6], "big-endian-flag (false = 0)");
        assertEquals((byte) 1, bytes[7], "true-colour-flag (true = 1)");
        // red-max = 255 (big-endian U16)
        assertEquals((byte) 0, bytes[8]);
        assertEquals((byte) 255, bytes[9]);
        // green-max = 255
        assertEquals((byte) 0, bytes[10]);
        assertEquals((byte) 255, bytes[11]);
        // blue-max = 255
        assertEquals((byte) 0, bytes[12]);
        assertEquals((byte) 255, bytes[13]);
        // red-shift = 16
        assertEquals((byte) 16, bytes[14]);
        // green-shift = 8
        assertEquals((byte) 8, bytes[15]);
        // blue-shift = 0
        assertEquals((byte) 0, bytes[16]);
        // padding (3 bytes)
        assertEquals((byte) 0, bytes[17]);
        assertEquals((byte) 0, bytes[18]);
        assertEquals((byte) 0, bytes[19]);
    }

    // -----------------------------------------------------------------------
    // SetEncodings
    // -----------------------------------------------------------------------

    /**
     * Verifies the SetEncodings message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - SetEncodings:
     *
     *   Sets the encoding types in which pixel data can be sent by the server.
     *
     *   =============== ==================== ========== =======================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== =======================
     *   1               U8                   2          message-type
     *   1                                               padding
     *   2               U16                             number-of-encodings
     *   =============== ==================== ========== =======================
     *
     *   followed by number-of-encodings repetitions of:
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   4               S32                             encoding-type
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testSetEncodings_byteFormat() throws IOException {
        // Encoding types: 0=Raw, 2=RRE, -239=Cursor pseudo-encoding
        SetEncodings msg = SetEncodings.newBuilder()
                .encodings(List.of(0, 2, -239))
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 1 + 1 + 2 + 3*4 = 16 bytes
        assertEquals(16, bytes.length);

        // byte 0: message-type = 2
        assertEquals((byte) 2, bytes[0], "message-type must be 2 for SetEncodings");
        // byte 1: padding
        assertEquals((byte) 0, bytes[1]);
        // bytes 2-3: number-of-encodings = 3 (big-endian U16)
        assertEquals((byte) 0, bytes[2]);
        assertEquals((byte) 3, bytes[3]);
        // bytes 4-7: encoding 0 (Raw) as big-endian S32: 0x00 0x00 0x00 0x00
        assertEquals((byte) 0, bytes[4]);
        assertEquals((byte) 0, bytes[5]);
        assertEquals((byte) 0, bytes[6]);
        assertEquals((byte) 0, bytes[7]);
        // bytes 8-11: encoding 2 (RRE) as big-endian S32: 0x00 0x00 0x00 0x02
        assertEquals((byte) 0, bytes[8]);
        assertEquals((byte) 0, bytes[9]);
        assertEquals((byte) 0, bytes[10]);
        assertEquals((byte) 2, bytes[11]);
        // bytes 12-15: encoding -239 as big-endian S32: 0xFF 0xFF 0xFF 0x11
        // -239 = 0xFFFFFF11
        assertEquals((byte) 0xFF, bytes[12]);
        assertEquals((byte) 0xFF, bytes[13]);
        assertEquals((byte) 0xFF, bytes[14]);
        assertEquals((byte) 0x11, bytes[15]);
    }

    /**
     * Verifies the SetEncodings message with an empty encoding list.
     */
    @Test
    void testSetEncodings_empty_byteFormat() throws IOException {
        SetEncodings msg = SetEncodings.newBuilder()
                .encodings(List.of())
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 1 + 1 + 2 = 4 bytes
        assertEquals(4, bytes.length);
        assertEquals((byte) 2, bytes[0]);  // message-type
        assertEquals((byte) 0, bytes[1]);  // padding
        assertEquals((byte) 0, bytes[2]);  // count high byte
        assertEquals((byte) 0, bytes[3]);  // count low byte
    }

    // -----------------------------------------------------------------------
    // FramebufferUpdateRequest
    // -----------------------------------------------------------------------

    /**
     * Verifies the FramebufferUpdateRequest message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - FramebufferUpdateRequest:
     *
     *   Notifies the server that the client is interested in the area of the
     *   framebuffer specified by x-position, y-position, width and height.
     *
     *   =============== ==================== ========== =======================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== =======================
     *   1               U8                   3          message-type
     *   1               U8                              incremental
     *   2               U16                             x-position
     *   2               U16                             y-position
     *   2               U16                             width
     *   2               U16                             height
     *   =============== ==================== ========== =======================
     * </pre>
     */
    @Test
    void testFramebufferUpdateRequest_incremental_byteFormat() throws IOException {
        FramebufferUpdateRequest msg = FramebufferUpdateRequest.newBuilder()
                .incremental(true)
                .x(100)
                .y(200)
                .width(320)
                .height(240)
                .build();
        byte[] bytes = serialize(msg::write);

        assertEquals(10, bytes.length, "FramebufferUpdateRequest must be exactly 10 bytes");

        // byte 0: message-type = 3
        assertEquals((byte) 3, bytes[0], "message-type must be 3");
        // byte 1: incremental = 1 (true)
        assertNotEquals((byte) 0, bytes[1], "incremental=true must be non-zero");
        // bytes 2-3: x-position = 100 (big-endian U16: 0x00 0x64)
        assertEquals((byte) 0x00, bytes[2]);
        assertEquals((byte) 0x64, bytes[3]);
        // bytes 4-5: y-position = 200 (big-endian U16: 0x00 0xC8)
        assertEquals((byte) 0x00, bytes[4]);
        assertEquals((byte) 0xC8, bytes[5]);
        // bytes 6-7: width = 320 (big-endian U16: 0x01 0x40)
        assertEquals((byte) 0x01, bytes[6]);
        assertEquals((byte) 0x40, bytes[7]);
        // bytes 8-9: height = 240 (big-endian U16: 0x00 0xF0)
        assertEquals((byte) 0x00, bytes[8]);
        assertEquals((byte) 0xF0, bytes[9]);
    }

    /**
     * Verifies the FramebufferUpdateRequest with incremental=false (full refresh).
     *
     * <pre>
     * From rfbproto.rst.txt - FramebufferUpdateRequest:
     *
     *   If for some reason the client has lost the contents of a particular area,
     *   the client sends a FramebufferUpdateRequest with incremental set to zero (false).
     *   This requests that the server send the entire contents of the specified area.
     * </pre>
     */
    @Test
    void testFramebufferUpdateRequest_nonIncremental_byteFormat() throws IOException {
        FramebufferUpdateRequest msg = FramebufferUpdateRequest.newBuilder()
                .incremental(false)
                .x(0).y(0).width(800).height(600)
                .build();
        byte[] bytes = serialize(msg::write);

        assertEquals(10, bytes.length);
        assertEquals((byte) 3, bytes[0]);  // message-type
        assertEquals((byte) 0, bytes[1]);  // incremental = 0 (false)
        // x = 0
        assertEquals((byte) 0, bytes[2]);
        assertEquals((byte) 0, bytes[3]);
        // y = 0
        assertEquals((byte) 0, bytes[4]);
        assertEquals((byte) 0, bytes[5]);
        // width = 800 (0x03 0x20)
        assertEquals((byte) 0x03, bytes[6]);
        assertEquals((byte) 0x20, bytes[7]);
        // height = 600 (0x02 0x58)
        assertEquals((byte) 0x02, bytes[8]);
        assertEquals((byte) 0x58, bytes[9]);
    }

    // -----------------------------------------------------------------------
    // KeyEvent
    // -----------------------------------------------------------------------

    /**
     * Verifies the KeyEvent message byte format for a key press.
     *
     * <pre>
     * From rfbproto.rst.txt - KeyEvent:
     *
     *   A key press or release. Down-flag is non-zero (true) if the key is now
     *   pressed, zero (false) if it is now released. The key itself is specified
     *   using the "keysym" values defined by the X Window System.
     *
     *   =============== ==================== ========== =======================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== =======================
     *   1               U8                   4          message-type
     *   1               U8                              down-flag
     *   2                                               padding
     *   4               U32                             key
     *   =============== ==================== ========== =======================
     * </pre>
     */
    @Test
    void testKeyEvent_down_byteFormat() throws IOException {
        // Key 'A' = 0x41 in X11 keysym
        KeyEvent msg = KeyEvent.newBuilder()
                .down(true)
                .key(0x41)
                .build();
        byte[] bytes = serialize(msg::write);

        assertEquals(8, bytes.length, "KeyEvent must be exactly 8 bytes");

        // byte 0: message-type = 4
        assertEquals((byte) 4, bytes[0], "message-type must be 4 for KeyEvent");
        // byte 1: down-flag = 1 (key pressed)
        assertNotEquals((byte) 0, bytes[1], "down=true must be non-zero");
        // bytes 2-3: padding
        assertEquals((byte) 0, bytes[2]);
        assertEquals((byte) 0, bytes[3]);
        // bytes 4-7: key = 0x41 (big-endian U32)
        assertEquals((byte) 0x00, bytes[4]);
        assertEquals((byte) 0x00, bytes[5]);
        assertEquals((byte) 0x00, bytes[6]);
        assertEquals((byte) 0x41, bytes[7]);
    }

    /**
     * Verifies the KeyEvent message byte format for a key release.
     *
     * <pre>
     * From rfbproto.rst.txt - KeyEvent:
     *
     *   Down-flag is non-zero (true) if the key is now pressed, zero (false) if
     *   it is now released.
     *
     *   Some common keysym values:
     *   BackSpace  0xff08
     *   Tab        0xff09
     *   Return     0xff0d
     *   Escape     0xff1b
     *   Delete     0xffff
     * </pre>
     */
    @Test
    void testKeyEvent_up_byteFormat() throws IOException {
        // Key Escape = 0xff1b
        KeyEvent msg = KeyEvent.newBuilder()
                .down(false)
                .key(0xff1b)
                .build();
        byte[] bytes = serialize(msg::write);

        assertEquals(8, bytes.length);
        assertEquals((byte) 4, bytes[0]);   // message-type
        assertEquals((byte) 0, bytes[1]);   // down=false
        assertEquals((byte) 0, bytes[2]);   // padding
        assertEquals((byte) 0, bytes[3]);   // padding
        // key = 0xff1b (big-endian)
        assertEquals((byte) 0x00, bytes[4]);
        assertEquals((byte) 0x00, bytes[5]);
        assertEquals((byte) 0xFF, bytes[6]);
        assertEquals((byte) 0x1B, bytes[7]);
    }

    /**
     * Verifies that a large keysym value (e.g., Shift Left = 0xffe1) is correctly
     * encoded as a big-endian U32.
     */
    @Test
    void testKeyEvent_largeKeysym_byteFormat() throws IOException {
        // Shift (left) = 0xffe1
        KeyEvent msg = KeyEvent.newBuilder()
                .down(true)
                .key(0xffe1)
                .build();
        byte[] bytes = serialize(msg::write);

        assertEquals(8, bytes.length);
        assertEquals((byte) 4, bytes[0]);
        // 0xffe1 as big-endian U32: 00 00 FF E1
        assertEquals((byte) 0x00, bytes[4]);
        assertEquals((byte) 0x00, bytes[5]);
        assertEquals((byte) 0xFF, bytes[6]);
        assertEquals((byte) 0xE1, bytes[7]);
    }

    // -----------------------------------------------------------------------
    // PointerEvent
    // -----------------------------------------------------------------------

    /**
     * Verifies the PointerEvent message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - PointerEvent:
     *
     *   Indicates either pointer movement or a pointer button press or release.
     *   The pointer is now at (x-position, y-position), and the current state of
     *   buttons 1 to 8 are represented by bits 0 to 7 of button-mask respectively,
     *   0 meaning up, 1 meaning down (pressed).
     *
     *   =============== ==================== ========== =======================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== =======================
     *   1               U8                   5          message-type
     *   1               U8                              button-mask
     *   2               U16                             x-position
     *   2               U16                             y-position
     *   =============== ==================== ========== =======================
     *
     *   Button bits:
     *   Bit 0 = Left button
     *   Bit 1 = Middle button
     *   Bit 2 = Right button
     *   Bit 3 = Scroll up
     *   Bit 4 = Scroll down
     * </pre>
     */
    @Test
    void testPointerEvent_byteFormat() throws IOException {
        // Left button pressed (bit 0), position (640, 480)
        PointerEvent msg = PointerEvent.newBuilder()
                .buttonMask(0x01)
                .x(640)
                .y(480)
                .build();
        byte[] bytes = serialize(msg::write);

        assertEquals(6, bytes.length, "PointerEvent must be exactly 6 bytes");

        // byte 0: message-type = 5
        assertEquals((byte) 5, bytes[0], "message-type must be 5 for PointerEvent");
        // byte 1: button-mask = 0x01 (left button pressed)
        assertEquals((byte) 0x01, bytes[1], "button-mask");
        // bytes 2-3: x-position = 640 (big-endian U16: 0x02 0x80)
        assertEquals((byte) 0x02, bytes[2]);
        assertEquals((byte) 0x80, bytes[3]);
        // bytes 4-5: y-position = 480 (big-endian U16: 0x01 0xE0)
        assertEquals((byte) 0x01, bytes[4]);
        assertEquals((byte) 0xE0, bytes[5]);
    }

    /**
     * Verifies a PointerEvent with no buttons pressed (pure movement).
     */
    @Test
    void testPointerEvent_noButtons_byteFormat() throws IOException {
        PointerEvent msg = PointerEvent.newBuilder()
                .buttonMask(0x00)
                .x(0)
                .y(0)
                .build();
        byte[] bytes = serialize(msg::write);

        assertEquals(6, bytes.length);
        assertEquals((byte) 5, bytes[0]);   // message-type
        assertEquals((byte) 0, bytes[1]);   // no buttons pressed
        assertEquals((byte) 0, bytes[2]);   // x = 0
        assertEquals((byte) 0, bytes[3]);
        assertEquals((byte) 0, bytes[4]);   // y = 0
        assertEquals((byte) 0, bytes[5]);
    }

    /**
     * Verifies that right-button press is encoded as bit 2 of the button-mask.
     *
     * <pre>
     * From rfbproto.rst.txt - PointerEvent:
     *
     *   On a conventional mouse, buttons 1, 2 and 3 correspond to the left, middle
     *   and right buttons on the mouse. The current state of buttons 1 to 8 are
     *   represented by bits 0 to 7 of button-mask respectively.
     * </pre>
     */
    @Test
    void testPointerEvent_rightButton_byteFormat() throws IOException {
        // Right button = bit 2 = 0x04
        PointerEvent msg = PointerEvent.newBuilder()
                .buttonMask(0x04)
                .x(100)
                .y(100)
                .build();
        byte[] bytes = serialize(msg::write);

        assertEquals((byte) 5, bytes[0]);
        assertEquals((byte) 0x04, bytes[1], "Right button is bit 2 (value 0x04)");
    }

    // -----------------------------------------------------------------------
    // ClientCutText
    // -----------------------------------------------------------------------

    /**
     * Verifies the ClientCutText message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - ClientCutText:
     *
     *   The client has new ISO 8859-1 (Latin-1) text in its cut buffer.
     *   Ends of lines are represented by the linefeed / newline character (value 10) alone.
     *
     *   =============== ==================== ========== =======================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== =======================
     *   1               U8                   6          message-type
     *   3                                               padding
     *   4               U32                             length
     *   length          U8 array                        text
     *   =============== ==================== ========== =======================
     * </pre>
     */
    @Test
    void testClientCutText_byteFormat() throws IOException {
        byte[] textBytes = "Hello".getBytes(StandardCharsets.ISO_8859_1);
        ClientCutText msg = ClientCutText.newBuilder()
                .text(textBytes)
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 1 + 3 + 4 + 5 = 13 bytes
        assertEquals(13, bytes.length);

        // byte 0: message-type = 6
        assertEquals((byte) 6, bytes[0], "message-type must be 6 for ClientCutText");
        // bytes 1-3: padding
        assertEquals((byte) 0, bytes[1]);
        assertEquals((byte) 0, bytes[2]);
        assertEquals((byte) 0, bytes[3]);
        // bytes 4-7: length = 5 (big-endian U32)
        assertEquals((byte) 0, bytes[4]);
        assertEquals((byte) 0, bytes[5]);
        assertEquals((byte) 0, bytes[6]);
        assertEquals((byte) 5, bytes[7]);
        // bytes 8-12: "Hello"
        assertEquals((byte) 'H', bytes[8]);
        assertEquals((byte) 'e', bytes[9]);
        assertEquals((byte) 'l', bytes[10]);
        assertEquals((byte) 'l', bytes[11]);
        assertEquals((byte) 'o', bytes[12]);
    }

    /**
     * Verifies the ClientCutText message with an empty text buffer.
     */
    @Test
    void testClientCutText_empty_byteFormat() throws IOException {
        ClientCutText msg = ClientCutText.newBuilder()
                .text(new byte[0])
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 1 + 3 + 4 + 0 = 8 bytes
        assertEquals(8, bytes.length);
        assertEquals((byte) 6, bytes[0]);
        assertEquals((byte) 0, bytes[1]);
        assertEquals((byte) 0, bytes[2]);
        assertEquals((byte) 0, bytes[3]);
        // length = 0
        assertEquals((byte) 0, bytes[4]);
        assertEquals((byte) 0, bytes[5]);
        assertEquals((byte) 0, bytes[6]);
        assertEquals((byte) 0, bytes[7]);
    }
}
