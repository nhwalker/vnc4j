package io.github.nhwalker.vnc4j.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying that extended / optional server-to-client messages conform to the
 * byte-level format specified in the RFB protocol specification (rfbproto.rst.txt).
 *
 * <p>Covers: EndOfContinuousUpdates, ServerFence, XvpServerMessage,
 * GiiServerVersion, GiiDeviceCreationResponse, QemuAudioServerEnd,
 * QemuAudioServerBegin, QemuAudioServerData.
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

    // -----------------------------------------------------------------------
    // GiiServerVersion
    // -----------------------------------------------------------------------

    /**
     * Verifies the GiiServerVersion message byte format (big-endian mode).
     *
     * <pre>
     * From rfbproto.rst.txt - GII Server Version:
     *
     *   Sent by the server in response to a GII Client Version message to
     *   advertise the supported version range.
     *
     *   =============== ==================== ========== ===========================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== ===========================
     *   1               U8                   253        message-type
     *   1               U8                   0x81/0x01  endian-and-sub-type (version)
     *   2               EU16                 4          length
     *   2               EU16                            maximum-version
     *   2               EU16                            minimum-version
     *   =============== ==================== ========== ===========================
     *
     *   The high bit of endian-and-sub-type indicates big-endian (0x81) or
     *   little-endian (0x01) byte order for all EU16/EU32 fields.
     * </pre>
     */
    @Test
    void testGiiServerVersion_bigEndian_byteFormat() throws IOException {
        GiiServerVersion msg = GiiServerVersion.newBuilder()
                .bigEndian(true)
                .maximumVersion(3)
                .minimumVersion(1)
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 1(type) + 1(endian+sub) + 2(length) + 2(maxVer) + 2(minVer) = 8 bytes
        assertEquals(8, bytes.length);
        assertEquals((byte) 253, bytes[0], "message-type must be 253 for GII");
        assertEquals((byte) 0x81, bytes[1], "big-endian version sub-type = 0x81");
        // length = 4 as big-endian U16
        assertEquals((byte) 0x00, bytes[2]);
        assertEquals((byte) 0x04, bytes[3]);
        // maximum-version = 3 as big-endian U16
        assertEquals((byte) 0x00, bytes[4]);
        assertEquals((byte) 0x03, bytes[5]);
        // minimum-version = 1 as big-endian U16
        assertEquals((byte) 0x00, bytes[6]);
        assertEquals((byte) 0x01, bytes[7]);
    }

    /**
     * Verifies the GiiServerVersion message byte format (little-endian mode).
     */
    @Test
    void testGiiServerVersion_littleEndian_byteFormat() throws IOException {
        GiiServerVersion msg = GiiServerVersion.newBuilder()
                .bigEndian(false)
                .maximumVersion(3)
                .minimumVersion(1)
                .build();
        byte[] bytes = serialize(msg::write);

        assertEquals(8, bytes.length);
        assertEquals((byte) 253, bytes[0]);
        assertEquals((byte) 0x01, bytes[1], "little-endian version sub-type = 0x01");
        // length = 4 as little-endian U16 (low byte first)
        assertEquals((byte) 0x04, bytes[2]);
        assertEquals((byte) 0x00, bytes[3]);
        // maximum-version = 3 as little-endian U16
        assertEquals((byte) 0x03, bytes[4]);
        assertEquals((byte) 0x00, bytes[5]);
        // minimum-version = 1 as little-endian U16
        assertEquals((byte) 0x01, bytes[6]);
        assertEquals((byte) 0x00, bytes[7]);
    }

    // -----------------------------------------------------------------------
    // GiiDeviceCreationResponse
    // -----------------------------------------------------------------------

    /**
     * Verifies the GiiDeviceCreationResponse message byte format (big-endian).
     *
     * <pre>
     * From rfbproto.rst.txt - GII Device Creation Response:
     *
     *   Sent by the server to inform the client of the deviceOrigin assigned
     *   to the newly created virtual input device (0 indicates failure).
     *
     *   =============== ==================== ========== ===========================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== ===========================
     *   1               U8                   253        message-type
     *   1               U8                   0x82/0x02  endian-and-sub-type (device creation)
     *   2               EU16                 4          length
     *   4               EU32                            device-origin
     *   =============== ==================== ========== ===========================
     * </pre>
     */
    @Test
    void testGiiDeviceCreationResponse_bigEndian_byteFormat() throws IOException {
        GiiDeviceCreationResponse msg = GiiDeviceCreationResponse.newBuilder()
                .bigEndian(true)
                .deviceOrigin(0x00000100L)
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 1(type) + 1(endian+sub) + 2(length) + 4(deviceOrigin) = 8 bytes
        assertEquals(8, bytes.length);
        assertEquals((byte) 253, bytes[0], "message-type must be 253 for GII");
        assertEquals((byte) 0x82, bytes[1], "big-endian device-creation sub-type = 0x82");
        // length = 4 as big-endian U16
        assertEquals((byte) 0x00, bytes[2]);
        assertEquals((byte) 0x04, bytes[3]);
        // device-origin = 0x00000100 as big-endian U32
        assertEquals((byte) 0x00, bytes[4]);
        assertEquals((byte) 0x00, bytes[5]);
        assertEquals((byte) 0x01, bytes[6]);
        assertEquals((byte) 0x00, bytes[7]);
    }

    // -----------------------------------------------------------------------
    // QemuAudioServerEnd
    // -----------------------------------------------------------------------

    /**
     * Verifies the QemuAudioServerEnd message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - QEMU Audio Server End:
     *
     *   Sent by the server to signal the end of an audio data stream.
     *
     *   =============== ==================== ========== =======================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== =======================
     *   1               U8                   255        message-type
     *   1               U8                   1          sub-type (audio)
     *   2               U16                  0          operation (end)
     *   =============== ==================== ========== =======================
     * </pre>
     */
    @Test
    void testQemuAudioServerEnd_byteFormat() throws IOException {
        QemuAudioServerEnd msg = QemuAudioServerEnd.newBuilder().build();
        byte[] bytes = serialize(msg::write);

        assertEquals(4, bytes.length, "QemuAudioServerEnd must be 4 bytes");
        assertEquals((byte) 255, bytes[0], "message-type must be 255 for QEMU");
        assertEquals((byte) 1, bytes[1], "sub-type must be 1 for audio");
        assertEquals((byte) 0, bytes[2], "operation high byte");
        assertEquals((byte) 0, bytes[3], "operation = 0 (end)");
    }

    // -----------------------------------------------------------------------
    // QemuAudioServerBegin
    // -----------------------------------------------------------------------

    /**
     * Verifies the QemuAudioServerBegin message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - QEMU Audio Server Begin:
     *
     *   Sent by the server to signal the start of an audio data stream.
     *
     *   =============== ==================== ========== =======================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== =======================
     *   1               U8                   255        message-type
     *   1               U8                   1          sub-type (audio)
     *   2               U16                  1          operation (begin)
     *   =============== ==================== ========== =======================
     * </pre>
     */
    @Test
    void testQemuAudioServerBegin_byteFormat() throws IOException {
        QemuAudioServerBegin msg = QemuAudioServerBegin.newBuilder().build();
        byte[] bytes = serialize(msg::write);

        assertEquals(4, bytes.length, "QemuAudioServerBegin must be 4 bytes");
        assertEquals((byte) 255, bytes[0], "message-type must be 255 for QEMU");
        assertEquals((byte) 1, bytes[1], "sub-type must be 1 for audio");
        assertEquals((byte) 0, bytes[2], "operation high byte");
        assertEquals((byte) 1, bytes[3], "operation = 1 (begin)");
    }

    // -----------------------------------------------------------------------
    // QemuAudioServerData
    // -----------------------------------------------------------------------

    /**
     * Verifies the QemuAudioServerData message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - QEMU Audio Server Data:
     *
     *   Carries a chunk of raw audio sample data.
     *
     *   =============== ==================== ========== =======================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== =======================
     *   1               U8                   255        message-type
     *   1               U8                   1          sub-type (audio)
     *   2               U16                  2          operation (data)
     *   4               U32                             length
     *   length          U8 array                        audio-data
     *   =============== ==================== ========== =======================
     * </pre>
     */
    @Test
    void testQemuAudioServerData_byteFormat() throws IOException {
        byte[] audioData = new byte[]{0x10, 0x20, 0x30};
        QemuAudioServerData msg = QemuAudioServerData.newBuilder()
                .data(audioData)
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 1 + 1 + 2 + 4 + 3 = 11 bytes
        assertEquals(11, bytes.length);
        assertEquals((byte) 255, bytes[0], "message-type must be 255 for QEMU");
        assertEquals((byte) 1, bytes[1], "sub-type must be 1 for audio");
        // operation = 2 as big-endian U16
        assertEquals((byte) 0, bytes[2]);
        assertEquals((byte) 2, bytes[3], "operation = 2 (data)");
        // length = 3 as big-endian U32
        assertEquals((byte) 0, bytes[4]);
        assertEquals((byte) 0, bytes[5]);
        assertEquals((byte) 0, bytes[6]);
        assertEquals((byte) 3, bytes[7]);
        // audio data
        assertEquals((byte) 0x10, bytes[8]);
        assertEquals((byte) 0x20, bytes[9]);
        assertEquals((byte) 0x30, bytes[10]);
    }

    /**
     * Verifies the QemuAudioServerData message with empty audio data.
     */
    @Test
    void testQemuAudioServerData_empty_byteFormat() throws IOException {
        QemuAudioServerData msg = QemuAudioServerData.newBuilder()
                .data(new byte[0])
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 1 + 1 + 2 + 4 = 8 bytes
        assertEquals(8, bytes.length);
        assertEquals((byte) 255, bytes[0]);
        assertEquals((byte) 1, bytes[1]);
        assertEquals((byte) 0, bytes[2]);
        assertEquals((byte) 2, bytes[3]);
        // length = 0
        assertEquals((byte) 0, bytes[4]);
        assertEquals((byte) 0, bytes[5]);
        assertEquals((byte) 0, bytes[6]);
        assertEquals((byte) 0, bytes[7]);
    }
}
