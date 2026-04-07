package io.github.nhwalker.vnc4j.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying that extended / optional server-to-client messages conform to the
 * byte-level format specified in the RFB protocol specification (rfbproto.rst.txt).
 *
 * <p>Covers: EndOfContinuousUpdates, ServerFence, XvpServerMessage.
 */
class ExtendedServerMessageByteFormatTest {

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
    // EndOfContinuousUpdates
    // -----------------------------------------------------------------------

    /**
     * Verifies the EndOfContinuousUpdates message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - EndOfContinuousUpdates:
     *
     *   Informs the client that the server has finished sending all of the
     *   rectangle updates that will be sent as a result of a
     *   EnableContinuousUpdates message.
     *
     *   =============== ==================== ========== =======================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== =======================
     *   1               U8                   150        message-type
     *   =============== ==================== ========== =======================
     * </pre>
     */
    @Test
    void testEndOfContinuousUpdates_byteFormat() throws IOException {
        EndOfContinuousUpdates msg = EndOfContinuousUpdates.newBuilder().build();
        byte[] bytes = serialize(msg::write);

        assertEquals(1, bytes.length, "EndOfContinuousUpdates must be exactly 1 byte");
        assertEquals((byte) 150, bytes[0], "message-type must be 150");
    }

    // -----------------------------------------------------------------------
    // ServerFence
    // -----------------------------------------------------------------------

    /**
     * Verifies the ServerFence message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - ServerFence:
     *
     *   A server-side fence message used for synchronization.
     *
     *   =============== ==================== ========== =======================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== =======================
     *   1               U8                   248        message-type
     *   3                                               padding
     *   4               U32                             flags
     *   1               U8                              length
     *   length          U8 array                        payload
     *   =============== ==================== ========== =======================
     * </pre>
     */
    @Test
    void testServerFence_byteFormat() throws IOException {
        byte[] payload = new byte[]{0x11, 0x22, 0x33};
        ServerFence msg = ServerFence.newBuilder()
                .flags(0x00000005)
                .payload(payload)
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 1 + 3 + 4 + 1 + 3 = 12 bytes
        assertEquals(12, bytes.length);

        assertEquals((byte) 248, bytes[0], "message-type must be 248 for ServerFence");
        assertEquals((byte) 0, bytes[1], "padding byte 1");
        assertEquals((byte) 0, bytes[2], "padding byte 2");
        assertEquals((byte) 0, bytes[3], "padding byte 3");
        // flags = 0x00000005 big-endian U32
        assertEquals((byte) 0x00, bytes[4]);
        assertEquals((byte) 0x00, bytes[5]);
        assertEquals((byte) 0x00, bytes[6]);
        assertEquals((byte) 0x05, bytes[7]);
        // length = 3
        assertEquals((byte) 3, bytes[8]);
        // payload
        assertEquals((byte) 0x11, bytes[9]);
        assertEquals((byte) 0x22, bytes[10]);
        assertEquals((byte) 0x33, bytes[11]);
    }

    /**
     * Verifies the ServerFence message with empty payload.
     */
    @Test
    void testServerFence_emptyPayload_byteFormat() throws IOException {
        ServerFence msg = ServerFence.newBuilder()
                .flags(0)
                .payload(new byte[0])
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 1 + 3 + 4 + 1 = 9 bytes
        assertEquals(9, bytes.length);
        assertEquals((byte) 248, bytes[0]);
        assertEquals((byte) 0, bytes[1]);
        assertEquals((byte) 0, bytes[2]);
        assertEquals((byte) 0, bytes[3]);
        assertEquals((byte) 0, bytes[4]);
        assertEquals((byte) 0, bytes[5]);
        assertEquals((byte) 0, bytes[6]);
        assertEquals((byte) 0, bytes[7]);
        assertEquals((byte) 0, bytes[8], "payload length = 0");
    }

    // -----------------------------------------------------------------------
    // XvpServerMessage
    // -----------------------------------------------------------------------

    /**
     * Verifies the XvpServerMessage byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - XvpServerMessage:
     *
     *   Used to announce server XVP capabilities or report action status.
     *
     *   =============== ==================== ========== =======================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== =======================
     *   1               U8                   250        message-type
     *   1                                               padding
     *   1               U8                              xvp-version
     *   1               U8                              xvp-message-code
     *   =============== ==================== ========== =======================
     * </pre>
     */
    @Test
    void testXvpServerMessage_byteFormat() throws IOException {
        XvpServerMessage msg = XvpServerMessage.newBuilder()
                .xvpVersion(1)
                .xvpMessageCode(2)
                .build();
        byte[] bytes = serialize(msg::write);

        assertEquals(4, bytes.length, "XvpServerMessage must be 4 bytes");
        assertEquals((byte) 250, bytes[0], "message-type must be 250 for XvpServerMessage");
        assertEquals((byte) 0, bytes[1], "padding");
        assertEquals((byte) 1, bytes[2], "xvp-version");
        assertEquals((byte) 2, bytes[3], "xvp-message-code");
    }

}
