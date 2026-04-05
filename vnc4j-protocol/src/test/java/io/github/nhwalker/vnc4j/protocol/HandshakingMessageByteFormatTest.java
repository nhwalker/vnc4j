package io.github.nhwalker.vnc4j.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying that handshaking and initialization messages conform to the
 * byte-level format specified in the RFB protocol specification (rfbproto.rst.txt).
 */
class HandshakingMessageByteFormatTest {

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
    // ProtocolVersion
    // -----------------------------------------------------------------------

    /**
     * Verifies the ProtocolVersion 3.8 message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - ProtocolVersion:
     *
     *   The ProtocolVersion message consists of 12 bytes interpreted as a string of
     *   ASCII characters in the format "RFB xxx.yyy\n" where xxx and yyy are the
     *   major and minor version numbers, padded with zeros.
     *
     *   ============= =========================================================
     *   No. of bytes  Value
     *   ============= =========================================================
     *   12            "RFB 003.008\n"
     *                 (hex 52 46 42 20 30 30 33 2e 30 30 38 0a)
     *   ============= =========================================================
     * </pre>
     */
    @Test
    void testProtocolVersion_38_byteFormat() throws IOException {
        ProtocolVersion msg = ProtocolVersion.newBuilder().major(3).minor(8).build();
        byte[] bytes = serialize(msg::write);

        byte[] expected = "RFB 003.008\n".getBytes(StandardCharsets.US_ASCII);
        assertArrayEquals(expected, bytes,
                "ProtocolVersion 3.8 must be exactly 12 ASCII bytes: 'RFB 003.008\\n'");
    }

    /**
     * Verifies the ProtocolVersion 3.7 message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - ProtocolVersion:
     *
     *   ============= =========================================================
     *   No. of bytes  Value
     *   ============= =========================================================
     *   12            "RFB 003.007\n"
     *                 (hex 52 46 42 20 30 30 33 2e 30 30 37 0a)
     *   ============= =========================================================
     * </pre>
     */
    @Test
    void testProtocolVersion_37_byteFormat() throws IOException {
        ProtocolVersion msg = ProtocolVersion.newBuilder().major(3).minor(7).build();
        byte[] bytes = serialize(msg::write);

        assertArrayEquals("RFB 003.007\n".getBytes(StandardCharsets.US_ASCII), bytes);
    }

    /**
     * Verifies the ProtocolVersion 3.3 message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - ProtocolVersion:
     *
     *   ============= =========================================================
     *   No. of bytes  Value
     *   ============= =========================================================
     *   12            "RFB 003.003\n"
     *                 (hex 52 46 42 20 30 30 33 2e 30 30 33 0a)
     *   ============= =========================================================
     * </pre>
     */
    @Test
    void testProtocolVersion_33_byteFormat() throws IOException {
        ProtocolVersion msg = ProtocolVersion.newBuilder().major(3).minor(3).build();
        byte[] bytes = serialize(msg::write);

        assertArrayEquals("RFB 003.003\n".getBytes(StandardCharsets.US_ASCII), bytes);
        assertEquals(12, bytes.length, "ProtocolVersion must always be exactly 12 bytes");
    }

    /**
     * Verifies the ProtocolVersion message is always exactly 12 bytes regardless of version.
     */
    @Test
    void testProtocolVersion_alwaysExactly12Bytes() throws IOException {
        ProtocolVersion msg = ProtocolVersion.newBuilder().major(3).minor(8).build();
        byte[] bytes = serialize(msg::write);
        assertEquals(12, bytes.length);
        // Must start with "RFB "
        assertEquals('R', bytes[0]);
        assertEquals('F', bytes[1]);
        assertEquals('B', bytes[2]);
        assertEquals(' ', bytes[3]);
        // Dot separator at position 7
        assertEquals('.', bytes[7]);
        // Newline terminator at position 11
        assertEquals('\n', bytes[11]);
    }

    // -----------------------------------------------------------------------
    // SecurityTypes (Server -> Client, RFB 3.7+)
    // -----------------------------------------------------------------------

    /**
     * Verifies the SecurityTypes message byte format for a non-empty list.
     *
     * <pre>
     * From rfbproto.rst.txt - Security (Version 3.7 onwards):
     *
     *   The server lists the security types which it supports:
     *
     *   ========================== ============= ==========================
     *   No. of bytes               Type          Description
     *   ========================== ============= ==========================
     *   1                          U8            number-of-security-types
     *   number-of-security-types   U8 array      security-types
     *   ========================== ============= ==========================
     * </pre>
     */
    @Test
    void testSecurityTypes_byteFormat() throws IOException {
        SecurityTypes msg = SecurityTypes.newBuilder()
                .securityTypes(List.of(1, 2))
                .build();
        byte[] bytes = serialize(msg::write);

        // byte 0: count = 2
        // byte 1: type 1 (None)
        // byte 2: type 2 (VNC Authentication)
        assertEquals(3, bytes.length);
        assertEquals((byte) 2, bytes[0], "First byte must be the number of security types");
        assertEquals((byte) 1, bytes[1], "Second byte must be security type 1 (None)");
        assertEquals((byte) 2, bytes[2], "Third byte must be security type 2 (VNC Authentication)");
    }

    /**
     * Verifies the SecurityTypes message with zero types encodes as a single zero byte.
     *
     * <pre>
     * From rfbproto.rst.txt - Security (Version 3.7 onwards):
     *
     *   If number-of-security-types is zero, then for some reason the connection
     *   failed. This is followed by a string describing the reason.
     *   (When writing, a count of zero indicates connection failure.)
     *
     *   ========================== ============= ==========================
     *   No. of bytes               Type          Description
     *   ========================== ============= ==========================
     *   1                          U8            number-of-security-types (= 0)
     *   ========================== ============= ==========================
     * </pre>
     */
    @Test
    void testSecurityTypes_empty_byteFormat() throws IOException {
        SecurityTypes msg = SecurityTypes.newBuilder()
                .securityTypes(List.of())
                .build();
        byte[] bytes = serialize(msg::write);

        assertEquals(1, bytes.length, "Empty security types list must produce exactly 1 byte (the zero count)");
        assertEquals((byte) 0, bytes[0], "Count byte must be 0 for empty list");
    }

    /**
     * Verifies that each security type occupies exactly 1 byte (U8).
     *
     * <pre>
     * From rfbproto.rst.txt - Security:
     *
     *   The security types defined are single U8 values:
     *   0  Invalid
     *   1  None
     *   2  VNC Authentication
     *   ...
     * </pre>
     */
    @Test
    void testSecurityTypes_eachTypeIsOneByte() throws IOException {
        SecurityTypes msg = SecurityTypes.newBuilder()
                .securityTypes(List.of(1, 2, 16, 19))
                .build();
        byte[] bytes = serialize(msg::write);

        // 1 byte count + 4 bytes (one per type)
        assertEquals(5, bytes.length);
        assertEquals((byte) 4, bytes[0]);
        assertEquals((byte) 1, bytes[1]);
        assertEquals((byte) 2, bytes[2]);
        assertEquals((byte) 16, bytes[3]);
        assertEquals((byte) 19, bytes[4]);
    }

    // -----------------------------------------------------------------------
    // SecurityTypeSelection (Client -> Server)
    // -----------------------------------------------------------------------

    /**
     * Verifies the SecurityTypeSelection message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - Security (Version 3.7 onwards):
     *
     *   If the server listed at least one valid security type supported by the client,
     *   the client sends back a single byte indicating which security type is to be used:
     *
     *   ========================== ============= ==========================
     *   No. of bytes               Type          Description
     *   ========================== ============= ==========================
     *   1                          U8            security-type
     *   ========================== ============= ==========================
     * </pre>
     */
    @Test
    void testSecurityTypeSelection_byteFormat() throws IOException {
        SecurityTypeSelection msg = SecurityTypeSelection.newBuilder()
                .securityType(2)
                .build();
        byte[] bytes = serialize(msg::write);

        assertEquals(1, bytes.length, "SecurityTypeSelection must be exactly 1 byte");
        assertEquals((byte) 2, bytes[0], "The single byte must be the selected security type");
    }

    /**
     * Verifies that SecurityTypeSelection for 'None' (type=1) is a single byte with value 1.
     */
    @Test
    void testSecurityTypeSelection_none_byteFormat() throws IOException {
        SecurityTypeSelection msg = SecurityTypeSelection.newBuilder()
                .securityType(1)
                .build();
        byte[] bytes = serialize(msg::write);

        assertEquals(1, bytes.length);
        assertEquals((byte) 1, bytes[0]);
    }

    // -----------------------------------------------------------------------
    // SecurityResult (Server -> Client)
    // -----------------------------------------------------------------------

    /**
     * Verifies the SecurityResult OK message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - SecurityResult:
     *
     *   The server sends a word to inform the client whether the security
     *   handshaking was successful.
     *
     *   =============== ======= =========== ===================================
     *   No. of bytes    Type    [Value]     Description
     *   =============== ======= =========== ===================================
     *   4               U32                 status:
     *   ..                      0           OK
     *   ..                      1           failed
     *   ..                      2           failed, too many attempts
     *   =============== ======= =========== ===================================
     * </pre>
     */
    @Test
    void testSecurityResult_ok_byteFormat() throws IOException {
        SecurityResult msg = SecurityResult.newBuilder()
                .status(0)
                .build();
        byte[] bytes = serialize(msg::write);

        assertEquals(4, bytes.length, "SecurityResult OK must be exactly 4 bytes");
        // Big-endian U32 value 0
        assertEquals((byte) 0, bytes[0]);
        assertEquals((byte) 0, bytes[1]);
        assertEquals((byte) 0, bytes[2]);
        assertEquals((byte) 0, bytes[3]);
    }

    /**
     * Verifies the SecurityResult failed message byte format with failure reason
     * (RFB 3.8+ behaviour).
     *
     * <pre>
     * From rfbproto.rst.txt - SecurityResult:
     *
     *   =============== ======= =========== ===================================
     *   No. of bytes    Type    [Value]     Description
     *   =============== ======= =========== ===================================
     *   4               U32     1           status: failed
     *   =============== ======= =========== ===================================
     *
     *   Version 3.8 onwards:
     *   If unsuccessful, the server sends a string describing the reason:
     *
     *   ========================== ============= ==========================
     *   No. of bytes               Type          Description
     *   ========================== ============= ==========================
     *   4                          U32           reason-length
     *   reason-length              U8 array      reason-string
     *   ========================== ============= ==========================
     * </pre>
     */
    @Test
    void testSecurityResult_failed_withReason_byteFormat() throws IOException {
        SecurityResult msg = SecurityResult.newBuilder()
                .status(1)
                .failureReason("Authentication failed")
                .build();
        byte[] bytes = serialize(msg::write);

        // status U32 = 1 (big-endian)
        assertEquals((byte) 0, bytes[0]);
        assertEquals((byte) 0, bytes[1]);
        assertEquals((byte) 0, bytes[2]);
        assertEquals((byte) 1, bytes[3]);

        // reason-length U32
        byte[] reasonBytes = "Authentication failed".getBytes(StandardCharsets.UTF_8);
        int reasonLen = reasonBytes.length;
        assertEquals((byte) 0, bytes[4]);
        assertEquals((byte) 0, bytes[5]);
        assertEquals((byte) 0, bytes[6]);
        assertEquals((byte) reasonLen, bytes[7]);

        // reason-string
        assertEquals(4 + 4 + reasonLen, bytes.length);
        byte[] actualReason = new byte[reasonLen];
        System.arraycopy(bytes, 8, actualReason, 0, reasonLen);
        assertArrayEquals(reasonBytes, actualReason);
    }

    /**
     * Verifies SecurityResult with status=1 and no failure reason encodes only the 4-byte status.
     */
    @Test
    void testSecurityResult_failed_withoutReason_byteFormat() throws IOException {
        SecurityResult msg = SecurityResult.newBuilder()
                .status(1)
                .build();
        byte[] bytes = serialize(msg::write);

        // Only the 4-byte status U32
        assertEquals(4, bytes.length);
        assertEquals((byte) 0, bytes[0]);
        assertEquals((byte) 0, bytes[1]);
        assertEquals((byte) 0, bytes[2]);
        assertEquals((byte) 1, bytes[3]);
    }

    // -----------------------------------------------------------------------
    // ClientInit
    // -----------------------------------------------------------------------

    /**
     * Verifies the ClientInit message byte format with shared=true.
     *
     * <pre>
     * From rfbproto.rst.txt - ClientInit:
     *
     *   =============== ======= ===============================================
     *   No. of bytes    Type    Description
     *   =============== ======= ===============================================
     *   1               U8      shared-flag
     *   =============== ======= ===============================================
     *
     *   Shared-flag is non-zero (true) if the server should try to share the
     *   desktop by leaving other clients connected.
     * </pre>
     */
    @Test
    void testClientInit_shared_byteFormat() throws IOException {
        ClientInit msg = ClientInit.newBuilder().shared(true).build();
        byte[] bytes = serialize(msg::write);

        assertEquals(1, bytes.length, "ClientInit must be exactly 1 byte");
        assertNotEquals((byte) 0, bytes[0], "shared=true must be non-zero");
    }

    /**
     * Verifies the ClientInit message byte format with shared=false.
     *
     * <pre>
     * From rfbproto.rst.txt - ClientInit:
     *
     *   =============== ======= ===============================================
     *   No. of bytes    Type    Description
     *   =============== ======= ===============================================
     *   1               U8      shared-flag
     *   =============== ======= ===============================================
     *
     *   Zero (false) means the server should give exclusive access by
     *   disconnecting all other clients.
     * </pre>
     */
    @Test
    void testClientInit_exclusive_byteFormat() throws IOException {
        ClientInit msg = ClientInit.newBuilder().shared(false).build();
        byte[] bytes = serialize(msg::write);

        assertEquals(1, bytes.length, "ClientInit must be exactly 1 byte");
        assertEquals((byte) 0, bytes[0], "shared=false must be zero");
    }

    // -----------------------------------------------------------------------
    // PixelFormat
    // -----------------------------------------------------------------------

    /**
     * Verifies the PixelFormat byte format (16 bytes total).
     *
     * <pre>
     * From rfbproto.rst.txt - ServerInit, PIXEL_FORMAT:
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
    void testPixelFormat_byteFormat() throws IOException {
        PixelFormat pf = PixelFormat.newBuilder()
                .bitsPerPixel(32)
                .depth(24)
                .bigEndian(false)
                .trueColour(true)
                .redMax(255)
                .greenMax(255)
                .blueMax(255)
                .redShift(16)
                .greenShift(8)
                .blueShift(0)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pf.write(baos);
        byte[] bytes = baos.toByteArray();

        assertEquals(16, bytes.length, "PixelFormat must always be exactly 16 bytes");

        // byte 0: bits-per-pixel
        assertEquals((byte) 32, bytes[0]);
        // byte 1: depth
        assertEquals((byte) 24, bytes[1]);
        // byte 2: big-endian-flag (0 = little endian)
        assertEquals((byte) 0, bytes[2]);
        // byte 3: true-colour-flag (1 = true colour)
        assertEquals((byte) 1, bytes[3]);
        // bytes 4-5: red-max = 255 (big-endian U16: 0x00, 0xFF)
        assertEquals((byte) 0, bytes[4]);
        assertEquals((byte) 255, bytes[5]);
        // bytes 6-7: green-max = 255
        assertEquals((byte) 0, bytes[6]);
        assertEquals((byte) 255, bytes[7]);
        // bytes 8-9: blue-max = 255
        assertEquals((byte) 0, bytes[8]);
        assertEquals((byte) 255, bytes[9]);
        // byte 10: red-shift
        assertEquals((byte) 16, bytes[10]);
        // byte 11: green-shift
        assertEquals((byte) 8, bytes[11]);
        // byte 12: blue-shift
        assertEquals((byte) 0, bytes[12]);
        // bytes 13-15: padding (must be zero)
        assertEquals((byte) 0, bytes[13]);
        assertEquals((byte) 0, bytes[14]);
        assertEquals((byte) 0, bytes[15]);
    }

    /**
     * Verifies that the big-endian-flag is correctly encoded as a non-zero byte when true.
     *
     * <pre>
     * From rfbproto.rst.txt - PixelFormat:
     *
     *   big-endian-flag is non-zero (true) if multi-byte pixels are interpreted as big endian.
     * </pre>
     */
    @Test
    void testPixelFormat_bigEndianFlag_byteFormat() throws IOException {
        PixelFormat pf = PixelFormat.newBuilder()
                .bitsPerPixel(16)
                .depth(16)
                .bigEndian(true)
                .trueColour(true)
                .redMax(31)
                .greenMax(63)
                .blueMax(31)
                .redShift(11)
                .greenShift(5)
                .blueShift(0)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pf.write(baos);
        byte[] bytes = baos.toByteArray();

        assertEquals(16, bytes.length);
        assertEquals((byte) 16, bytes[0]); // bits-per-pixel
        assertEquals((byte) 16, bytes[1]); // depth
        assertNotEquals((byte) 0, bytes[2], "big-endian-flag=true must be non-zero");
        assertNotEquals((byte) 0, bytes[3], "true-colour-flag=true must be non-zero");
    }

    // -----------------------------------------------------------------------
    // ServerInit
    // -----------------------------------------------------------------------

    /**
     * Verifies the ServerInit message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - ServerInit:
     *
     *   =============== =================== ===================================
     *   No. of bytes    Type                Description
     *   =============== =================== ===================================
     *   2               U16                 framebuffer-width
     *   2               U16                 framebuffer-height
     *   16              PIXEL_FORMAT        server-pixel-format
     *   4               U32                 name-length
     *   name-length     U8 array            name-string
     *   =============== =================== ===================================
     * </pre>
     */
    @Test
    void testServerInit_byteFormat() throws IOException {
        PixelFormat pf = PixelFormat.newBuilder()
                .bitsPerPixel(32).depth(24)
                .bigEndian(false).trueColour(true)
                .redMax(255).greenMax(255).blueMax(255)
                .redShift(16).greenShift(8).blueShift(0)
                .build();
        ServerInit msg = ServerInit.newBuilder()
                .framebufferWidth(1024)
                .framebufferHeight(768)
                .pixelFormat(pf)
                .name("TestDesktop")
                .build();
        byte[] bytes = serialize(msg::write);

        // bytes 0-1: framebuffer-width = 1024 (big-endian U16: 0x04 0x00)
        assertEquals((byte) 0x04, bytes[0]);
        assertEquals((byte) 0x00, bytes[1]);
        // bytes 2-3: framebuffer-height = 768 (big-endian U16: 0x03 0x00)
        assertEquals((byte) 0x03, bytes[2]);
        assertEquals((byte) 0x00, bytes[3]);
        // bytes 4-19: pixel-format (16 bytes)
        assertEquals((byte) 32, bytes[4]); // bits-per-pixel
        // bytes 20-23: name-length (big-endian U32)
        byte[] nameBytes = "TestDesktop".getBytes(StandardCharsets.UTF_8);
        assertEquals((byte) 0, bytes[20]);
        assertEquals((byte) 0, bytes[21]);
        assertEquals((byte) 0, bytes[22]);
        assertEquals((byte) nameBytes.length, bytes[23]);
        // bytes 24+: name-string
        assertEquals(2 + 2 + 16 + 4 + nameBytes.length, bytes.length);
        byte[] actualName = new byte[nameBytes.length];
        System.arraycopy(bytes, 24, actualName, 0, nameBytes.length);
        assertArrayEquals(nameBytes, actualName, "Name string must be UTF-8 encoded");
    }

    /**
     * Verifies that ServerInit with an empty name encodes a zero-length name field.
     */
    @Test
    void testServerInit_emptyName_byteFormat() throws IOException {
        PixelFormat pf = PixelFormat.newBuilder()
                .bitsPerPixel(8).depth(8)
                .bigEndian(false).trueColour(false)
                .redMax(0).greenMax(0).blueMax(0)
                .redShift(0).greenShift(0).blueShift(0)
                .build();
        ServerInit msg = ServerInit.newBuilder()
                .framebufferWidth(800).framebufferHeight(600)
                .pixelFormat(pf).name("")
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 2 + 2 + 16 + 4 + 0 = 24 bytes
        assertEquals(24, bytes.length);
        // name-length = 0
        assertEquals((byte) 0, bytes[20]);
        assertEquals((byte) 0, bytes[21]);
        assertEquals((byte) 0, bytes[22]);
        assertEquals((byte) 0, bytes[23]);
    }
}
