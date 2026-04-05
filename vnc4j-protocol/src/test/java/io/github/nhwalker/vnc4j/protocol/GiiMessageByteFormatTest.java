package io.github.nhwalker.vnc4j.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying that GII (Generic Input Interface) messages conform to the
 * byte-level format specified in the RFB protocol specification (rfbproto.rst.txt).
 *
 * <p>GII messages all use message-type 253. The second byte is an endian-and-sub-type byte:
 * the high bit (0x80) indicates big-endian byte order, and the lower 7 bits are the sub-type.
 * All EU16 and EU32 fields use the endianness indicated by this byte.
 *
 * <pre>
 * From rfbproto.rst.txt - GII Messages:
 *
 *   =============== ==================== ========== =======================
 *   No. of bytes    Type                 [Value]    Description
 *   =============== ==================== ========== =======================
 *   1               U8                   253        message-type
 *   1               U8                              endian-and-sub-type
 *   2               EU16                            length (payload length)
 *   length          U8 array                        payload
 *   =============== ==================== ========== =======================
 *
 *   endian-and-sub-type: bit 7 = 1 for big-endian, bit 7 = 0 for little-endian
 *                        bits 0-6 = sub-type
 *
 *   Sub-types:
 *     0 = InjectEvents
 *     1 = ClientVersion / ServerVersion
 *     2 = DeviceCreation / DeviceCreationResponse
 *     3 = DeviceDestruction
 * </pre>
 */
class GiiMessageByteFormatTest {

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
    // GiiClientVersion
    // -----------------------------------------------------------------------

    /**
     * Verifies the GiiClientVersion message byte format (big-endian mode).
     *
     * <pre>
     * From rfbproto.rst.txt - GII Client Version:
     *
     *   Sent by the client to initiate GII version negotiation.
     *
     *   =============== ==================== ========== ===========================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== ===========================
     *   1               U8                   253        message-type
     *   1               U8                   0x81/0x01  endian-and-sub-type
     *   2               EU16                 2          length (always 2)
     *   2               EU16                            version
     *   =============== ==================== ========== ===========================
     * </pre>
     */
    @Test
    void testGiiClientVersion_bigEndian_byteFormat() throws IOException {
        GiiClientVersion msg = GiiClientVersion.newBuilder()
                .bigEndian(true)
                .version(1)
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 1(type) + 1(endian+sub) + 2(length) + 2(version) = 6 bytes
        assertEquals(6, bytes.length);
        assertEquals((byte) 253, bytes[0], "message-type must be 253 for GII");
        assertEquals((byte) 0x81, bytes[1], "big-endian + sub-type 1 = 0x81");
        // length = 2 as big-endian U16
        assertEquals((byte) 0x00, bytes[2]);
        assertEquals((byte) 0x02, bytes[3]);
        // version = 1 as big-endian U16
        assertEquals((byte) 0x00, bytes[4]);
        assertEquals((byte) 0x01, bytes[5]);
    }

    /**
     * Verifies the GiiClientVersion message byte format (little-endian mode).
     */
    @Test
    void testGiiClientVersion_littleEndian_byteFormat() throws IOException {
        GiiClientVersion msg = GiiClientVersion.newBuilder()
                .bigEndian(false)
                .version(1)
                .build();
        byte[] bytes = serialize(msg::write);

        assertEquals(6, bytes.length);
        assertEquals((byte) 253, bytes[0], "message-type must be 253 for GII");
        assertEquals((byte) 0x01, bytes[1], "little-endian + sub-type 1 = 0x01");
        // length = 2 as little-endian U16 (LSB first)
        assertEquals((byte) 0x02, bytes[2]);
        assertEquals((byte) 0x00, bytes[3]);
        // version = 1 as little-endian U16
        assertEquals((byte) 0x01, bytes[4]);
        assertEquals((byte) 0x00, bytes[5]);
    }

    // -----------------------------------------------------------------------
    // GiiDeviceDestruction
    // -----------------------------------------------------------------------

    /**
     * Verifies the GiiDeviceDestruction message byte format (big-endian mode).
     *
     * <pre>
     * From rfbproto.rst.txt - GII DeviceDestruction:
     *
     *   Sent by the client to request destruction of a previously created device.
     *
     *   =============== ==================== ========== ===========================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== ===========================
     *   1               U8                   253        message-type
     *   1               U8                   0x83/0x03  endian-and-sub-type
     *   2               EU16                 4          length (always 4)
     *   4               EU32                            device-origin
     *   =============== ==================== ========== ===========================
     * </pre>
     */
    @Test
    void testGiiDeviceDestruction_bigEndian_byteFormat() throws IOException {
        GiiDeviceDestruction msg = GiiDeviceDestruction.newBuilder()
                .bigEndian(true)
                .deviceOrigin(0x00000002L)
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 1(type) + 1(endian+sub) + 2(length) + 4(deviceOrigin) = 8 bytes
        assertEquals(8, bytes.length);
        assertEquals((byte) 253, bytes[0], "message-type must be 253 for GII");
        assertEquals((byte) 0x83, bytes[1], "big-endian + sub-type 3 = 0x83");
        // length = 4 as big-endian U16
        assertEquals((byte) 0x00, bytes[2]);
        assertEquals((byte) 0x04, bytes[3]);
        // device-origin = 2 as big-endian U32
        assertEquals((byte) 0x00, bytes[4]);
        assertEquals((byte) 0x00, bytes[5]);
        assertEquals((byte) 0x00, bytes[6]);
        assertEquals((byte) 0x02, bytes[7]);
    }

    /**
     * Verifies the GiiDeviceDestruction message byte format (little-endian mode).
     */
    @Test
    void testGiiDeviceDestruction_littleEndian_byteFormat() throws IOException {
        GiiDeviceDestruction msg = GiiDeviceDestruction.newBuilder()
                .bigEndian(false)
                .deviceOrigin(0x00000002L)
                .build();
        byte[] bytes = serialize(msg::write);

        assertEquals(8, bytes.length);
        assertEquals((byte) 0x03, bytes[1], "little-endian + sub-type 3 = 0x03");
        // length = 4 as little-endian U16
        assertEquals((byte) 0x04, bytes[2]);
        assertEquals((byte) 0x00, bytes[3]);
        // device-origin = 2 as little-endian U32
        assertEquals((byte) 0x02, bytes[4]);
        assertEquals((byte) 0x00, bytes[5]);
        assertEquals((byte) 0x00, bytes[6]);
        assertEquals((byte) 0x00, bytes[7]);
    }

    // -----------------------------------------------------------------------
    // GiiInjectEvents with GiiKeyEvent
    // -----------------------------------------------------------------------

    /**
     * Verifies the GiiInjectEvents message byte format with a single GiiKeyEvent (big-endian).
     *
     * <pre>
     * From rfbproto.rst.txt - GII InjectEvents:
     *
     *   Sent by the client to inject input events into the server.
     *   The sub-type for InjectEvents is 0.
     *
     *   =============== ==================== ========== ===========================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== ===========================
     *   1               U8                   253        message-type
     *   1               U8                   0x80/0x00  endian-and-sub-type
     *   2               EU16                            length (total event bytes)
     *   length          U8 array                        events
     *   =============== ==================== ========== ===========================
     *
     *   Each GiiKeyEvent is 24 bytes:
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   1               U8                   24         event-size
     *   1               U8                              event-type
     *   2               EU16                            padding
     *   4               EU32                            device-origin
     *   4               EU32                            modifiers
     *   4               EU32                            symbol
     *   4               EU32                            label
     *   4               EU32                            button
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testGiiInjectEvents_withKeyEvent_bigEndian_byteFormat() throws IOException {
        GiiKeyEvent keyEvent = GiiKeyEvent.newBuilder()
                .eventType(1)
                .deviceOrigin(10L)
                .modifiers(0L)
                .symbol(0x61L)  // 'a' key
                .label(0L)
                .button(0L)
                .build();
        GiiInjectEvents msg = GiiInjectEvents.newBuilder()
                .bigEndian(true)
                .events(List.of(keyEvent))
                .build();
        byte[] bytes = serialize(msg::write);

        // Header: 1(type) + 1(endian+sub) + 2(length) = 4 bytes
        // One key event: 24 bytes
        // Total: 28 bytes
        assertEquals(28, bytes.length);
        assertEquals((byte) 253, bytes[0], "message-type must be 253 for GII");
        assertEquals((byte) 0x80, bytes[1], "big-endian + sub-type 0 = 0x80");
        // length = 24 as big-endian U16
        assertEquals((byte) 0x00, bytes[2]);
        assertEquals((byte) 24, bytes[3]);

        // Key event starts at byte 4
        assertEquals((byte) 24, bytes[4], "event-size = 24");
        assertEquals((byte) 1, bytes[5], "event-type");
        // padding (U16) = 0
        assertEquals((byte) 0, bytes[6]);
        assertEquals((byte) 0, bytes[7]);
        // device-origin = 10 as big-endian U32
        assertEquals((byte) 0, bytes[8]);
        assertEquals((byte) 0, bytes[9]);
        assertEquals((byte) 0, bytes[10]);
        assertEquals((byte) 10, bytes[11]);
        // modifiers = 0
        assertEquals((byte) 0, bytes[12]);
        assertEquals((byte) 0, bytes[13]);
        assertEquals((byte) 0, bytes[14]);
        assertEquals((byte) 0, bytes[15]);
        // symbol = 0x61
        assertEquals((byte) 0, bytes[16]);
        assertEquals((byte) 0, bytes[17]);
        assertEquals((byte) 0, bytes[18]);
        assertEquals((byte) 0x61, bytes[19]);
        // label = 0
        assertEquals((byte) 0, bytes[20]);
        assertEquals((byte) 0, bytes[21]);
        assertEquals((byte) 0, bytes[22]);
        assertEquals((byte) 0, bytes[23]);
        // button = 0
        assertEquals((byte) 0, bytes[24]);
        assertEquals((byte) 0, bytes[25]);
        assertEquals((byte) 0, bytes[26]);
        assertEquals((byte) 0, bytes[27]);
    }

    // -----------------------------------------------------------------------
    // GiiInjectEvents with GiiPointerButtonEvent
    // -----------------------------------------------------------------------

    /**
     * Verifies the GiiPointerButtonEvent byte format when embedded in GiiInjectEvents (big-endian).
     *
     * <pre>
     * From rfbproto.rst.txt - GII PointerButtonEvent:
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   1               U8                   12         event-size
     *   1               U8                              event-type
     *   2               EU16                            padding
     *   4               EU32                            device-origin
     *   4               EU32                            button-number
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testGiiInjectEvents_withPointerButtonEvent_bigEndian_byteFormat() throws IOException {
        GiiPointerButtonEvent buttonEvent = GiiPointerButtonEvent.newBuilder()
                .eventType(10)
                .deviceOrigin(5L)
                .buttonNumber(1L)
                .build();
        GiiInjectEvents msg = GiiInjectEvents.newBuilder()
                .bigEndian(true)
                .events(List.of(buttonEvent))
                .build();
        byte[] bytes = serialize(msg::write);

        // Header: 4 bytes + one button event: 12 bytes = 16 bytes
        assertEquals(16, bytes.length);
        assertEquals((byte) 253, bytes[0]);
        assertEquals((byte) 0x80, bytes[1]);
        // length = 12 as big-endian U16
        assertEquals((byte) 0x00, bytes[2]);
        assertEquals((byte) 12, bytes[3]);

        // Button event at byte 4
        assertEquals((byte) 12, bytes[4], "event-size = 12");
        assertEquals((byte) 10, bytes[5], "event-type = 10");
        // padding
        assertEquals((byte) 0, bytes[6]);
        assertEquals((byte) 0, bytes[7]);
        // device-origin = 5
        assertEquals((byte) 0, bytes[8]);
        assertEquals((byte) 0, bytes[9]);
        assertEquals((byte) 0, bytes[10]);
        assertEquals((byte) 5, bytes[11]);
        // button-number = 1
        assertEquals((byte) 0, bytes[12]);
        assertEquals((byte) 0, bytes[13]);
        assertEquals((byte) 0, bytes[14]);
        assertEquals((byte) 1, bytes[15]);
    }

    // -----------------------------------------------------------------------
    // GiiInjectEvents with GiiValuatorEvent
    // -----------------------------------------------------------------------

    /**
     * Verifies the GiiValuatorEvent byte format when embedded in GiiInjectEvents (big-endian).
     *
     * <pre>
     * From rfbproto.rst.txt - GII ValuatorEvent:
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   1               U8                   16+4*n     event-size
     *   1               U8                              event-type
     *   2               EU16                            padding
     *   4               EU32                            device-origin
     *   4               EU32                            first (first valuator index)
     *   4               EU32                            count
     *   4*count         ES32 array                      values
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testGiiInjectEvents_withValuatorEvent_bigEndian_byteFormat() throws IOException {
        GiiValuatorEvent valEvent = GiiValuatorEvent.newBuilder()
                .eventType(12)
                .deviceOrigin(3L)
                .first(0L)
                .values(List.of(100, -50))
                .build();
        GiiInjectEvents msg = GiiInjectEvents.newBuilder()
                .bigEndian(true)
                .events(List.of(valEvent))
                .build();
        byte[] bytes = serialize(msg::write);

        // event-size = 16 + 4*2 = 24 bytes; header = 4 bytes; total = 28
        assertEquals(28, bytes.length);
        assertEquals((byte) 253, bytes[0]);
        assertEquals((byte) 0x80, bytes[1]);
        // length = 24 as big-endian U16
        assertEquals((byte) 0x00, bytes[2]);
        assertEquals((byte) 24, bytes[3]);

        // Valuator event at byte 4
        assertEquals((byte) 24, bytes[4], "event-size = 24 (16 + 4*2)");
        assertEquals((byte) 12, bytes[5], "event-type = 12");
        assertEquals((byte) 0, bytes[6]);
        assertEquals((byte) 0, bytes[7]);
        // device-origin = 3
        assertEquals((byte) 0, bytes[8]);
        assertEquals((byte) 0, bytes[9]);
        assertEquals((byte) 0, bytes[10]);
        assertEquals((byte) 3, bytes[11]);
        // first = 0
        assertEquals((byte) 0, bytes[12]);
        assertEquals((byte) 0, bytes[13]);
        assertEquals((byte) 0, bytes[14]);
        assertEquals((byte) 0, bytes[15]);
        // count = 2
        assertEquals((byte) 0, bytes[16]);
        assertEquals((byte) 0, bytes[17]);
        assertEquals((byte) 0, bytes[18]);
        assertEquals((byte) 2, bytes[19]);
        // value[0] = 100 as big-endian S32
        assertEquals((byte) 0, bytes[20]);
        assertEquals((byte) 0, bytes[21]);
        assertEquals((byte) 0, bytes[22]);
        assertEquals((byte) 100, bytes[23]);
        // value[1] = -50 as big-endian S32: 0xFFFFFFCE
        assertEquals((byte) 0xFF, bytes[24]);
        assertEquals((byte) 0xFF, bytes[25]);
        assertEquals((byte) 0xFF, bytes[26]);
        assertEquals((byte) 0xCE, bytes[27]);
    }

    // -----------------------------------------------------------------------
    // GiiInjectEvents with GiiPointerMoveEvent
    // -----------------------------------------------------------------------

    /**
     * Verifies the GiiPointerMoveEvent byte format when embedded in GiiInjectEvents (big-endian).
     *
     * <pre>
     * From rfbproto.rst.txt - GII PointerMoveEvent:
     *
     *   =============== =============================== =======================
     *   No. of bytes    Type                            Description
     *   =============== =============================== =======================
     *   1               U8                   24         event-size
     *   1               U8                              event-type
     *   2               EU16                            padding
     *   4               EU32                            device-origin
     *   4               ES32                            x
     *   4               ES32                            y
     *   4               ES32                            z
     *   4               ES32                            wheel
     *   =============== =============================== =======================
     * </pre>
     */
    @Test
    void testGiiInjectEvents_withPointerMoveEvent_bigEndian_byteFormat() throws IOException {
        GiiPointerMoveEvent moveEvent = GiiPointerMoveEvent.newBuilder()
                .eventType(8)
                .deviceOrigin(1L)
                .x(200).y(150).z(0).wheel(0)
                .build();
        GiiInjectEvents msg = GiiInjectEvents.newBuilder()
                .bigEndian(true)
                .events(List.of(moveEvent))
                .build();
        byte[] bytes = serialize(msg::write);

        // Header: 4 bytes + move event: 24 bytes = 28 bytes
        assertEquals(28, bytes.length);
        assertEquals((byte) 253, bytes[0]);
        assertEquals((byte) 0x80, bytes[1]);
        assertEquals((byte) 0x00, bytes[2]);
        assertEquals((byte) 24, bytes[3]);

        assertEquals((byte) 24, bytes[4], "event-size = 24");
        assertEquals((byte) 8, bytes[5], "event-type = 8");
        assertEquals((byte) 0, bytes[6]);
        assertEquals((byte) 0, bytes[7]);
        // device-origin = 1
        assertEquals((byte) 0, bytes[8]);
        assertEquals((byte) 0, bytes[9]);
        assertEquals((byte) 0, bytes[10]);
        assertEquals((byte) 1, bytes[11]);
        // x = 200 as big-endian S32
        assertEquals((byte) 0, bytes[12]);
        assertEquals((byte) 0, bytes[13]);
        assertEquals((byte) 0, bytes[14]);
        assertEquals((byte) 200, bytes[15]);
        // y = 150 as big-endian S32
        assertEquals((byte) 0, bytes[16]);
        assertEquals((byte) 0, bytes[17]);
        assertEquals((byte) 0, bytes[18]);
        assertEquals((byte) 150, bytes[19]);
        // z = 0
        assertEquals((byte) 0, bytes[20]);
        assertEquals((byte) 0, bytes[21]);
        assertEquals((byte) 0, bytes[22]);
        assertEquals((byte) 0, bytes[23]);
        // wheel = 0
        assertEquals((byte) 0, bytes[24]);
        assertEquals((byte) 0, bytes[25]);
        assertEquals((byte) 0, bytes[26]);
        assertEquals((byte) 0, bytes[27]);
    }
}
