package io.github.nhwalker.vnc4j.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying that the GiiDeviceCreation message and GiiValuator
 * sub-structure conform to the byte-level format specified in the RFB protocol
 * specification (rfbproto.rst.txt).
 *
 * <pre>
 * From rfbproto.rst.txt - GII DeviceCreation:
 *
 *   Sent by the client to request creation of a virtual input device.
 *
 *   =============== ==================== ========== ===========================
 *   No. of bytes    Type                 [Value]    Description
 *   =============== ==================== ========== ===========================
 *   1               U8                   253        message-type
 *   1               U8                   0x82/0x02  endian-and-sub-type (device-creation)
 *   2               EU16                            length (56 + numValuators*116)
 *   32              U8 array                        device-name (31 bytes + NUL)
 *   4               EU32                            vendor-id
 *   4               EU32                            product-id
 *   4               EU32                            can-generate
 *   4               EU32                            num-registers
 *   4               EU32                            num-valuators
 *   4               EU32                            num-buttons
 *   =============== ==================== ========== ===========================
 *
 *   Followed by num-valuators repetitions of the 116-byte GiiValuator structure.
 *
 *   GiiValuator structure (116 bytes):
 *   =============== =============================== =======================
 *   No. of bytes    Type                            Description
 *   =============== =============================== =======================
 *   4               EU32                            index
 *   75              U8 array                        long-name (74 bytes + NUL)
 *   5               U8 array                        short-name (4 bytes + NUL)
 *   4               ES32                            range-min
 *   4               ES32                            range-center
 *   4               ES32                            range-max
 *   4               EU32                            si-unit
 *   4               ES32                            si-add
 *   4               ES32                            si-mul
 *   4               ES32                            si-div
 *   4               ES32                            si-shift
 *   =============== =============================== =======================
 * </pre>
 */
class GiiDeviceCreationByteFormatTest {

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
    // GiiValuator structure
    // -----------------------------------------------------------------------

    /**
     * Verifies the GiiValuator structure byte format (116 bytes total, big-endian).
     */
    @Test
    void testGiiValuator_byteSize() throws IOException {
        GiiValuator val = GiiValuator.newBuilder()
                .index(0L)
                .longName("X Axis")
                .shortName("X")
                .rangeMin(-1000)
                .rangeCenter(0)
                .rangeMax(1000)
                .siUnit(0L)
                .siAdd(0).siMul(1).siDiv(1).siShift(0)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        val.write(baos, true);
        byte[] bytes = baos.toByteArray();

        // GiiValuator is always 116 bytes
        assertEquals(116, bytes.length, "GiiValuator must always be 116 bytes");
    }

    /**
     * Verifies the GiiValuator structure field layout (big-endian).
     */
    @Test
    void testGiiValuator_bigEndian_fieldLayout() throws IOException {
        GiiValuator val = GiiValuator.newBuilder()
                .index(3L)
                .longName("X")
                .shortName("X")
                .rangeMin(-100)
                .rangeCenter(0)
                .rangeMax(100)
                .siUnit(2L)
                .siAdd(0).siMul(1).siDiv(1).siShift(0)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        val.write(baos, true);
        byte[] bytes = baos.toByteArray();

        assertEquals(116, bytes.length);

        // index = 3 as big-endian EU32 at offset 0
        assertEquals((byte) 0, bytes[0]);
        assertEquals((byte) 0, bytes[1]);
        assertEquals((byte) 0, bytes[2]);
        assertEquals((byte) 3, bytes[3]);

        // longName: 75 bytes (74 content + 1 NUL) at offset 4
        // "X" → 0x58 followed by zeros
        assertEquals((byte) 0x58, bytes[4], "long-name 'X' first byte");
        assertEquals((byte) 0, bytes[5], "long-name padding");
        // NUL at the end of longName field
        assertEquals((byte) 0, bytes[78], "long-name NUL terminator");

        // shortName: 5 bytes (4 content + 1 NUL) at offset 79
        assertEquals((byte) 0x58, bytes[79], "short-name 'X' first byte");
        assertEquals((byte) 0, bytes[83], "short-name NUL terminator");

        // rangeMin = -100 = 0xFFFFFF9C at offset 84 (big-endian S32)
        assertEquals((byte) 0xFF, bytes[84]);
        assertEquals((byte) 0xFF, bytes[85]);
        assertEquals((byte) 0xFF, bytes[86]);
        assertEquals((byte) 0x9C, bytes[87]);

        // rangeCenter = 0 at offset 88
        assertEquals((byte) 0, bytes[88]);
        assertEquals((byte) 0, bytes[89]);
        assertEquals((byte) 0, bytes[90]);
        assertEquals((byte) 0, bytes[91]);

        // rangeMax = 100 at offset 92
        assertEquals((byte) 0, bytes[92]);
        assertEquals((byte) 0, bytes[93]);
        assertEquals((byte) 0, bytes[94]);
        assertEquals((byte) 100, bytes[95]);

        // siUnit = 2 (EU32) at offset 96
        assertEquals((byte) 0, bytes[96]);
        assertEquals((byte) 0, bytes[97]);
        assertEquals((byte) 0, bytes[98]);
        assertEquals((byte) 2, bytes[99]);
    }

    /**
     * Verifies the GiiValuator structure field layout (little-endian).
     */
    @Test
    void testGiiValuator_littleEndian_fieldLayout() throws IOException {
        GiiValuator val = GiiValuator.newBuilder()
                .index(3L)
                .longName("").shortName("")
                .rangeMin(0).rangeCenter(0).rangeMax(256)
                .siUnit(0L).siAdd(0).siMul(1).siDiv(1).siShift(0)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        val.write(baos, false);  // little-endian
        byte[] bytes = baos.toByteArray();

        assertEquals(116, bytes.length);

        // index = 3 as little-endian EU32
        assertEquals((byte) 3, bytes[0], "index low byte");
        assertEquals((byte) 0, bytes[1]);
        assertEquals((byte) 0, bytes[2]);
        assertEquals((byte) 0, bytes[3], "index high byte");

        // rangeMax = 256 = 0x00000100 at offset 92, little-endian
        assertEquals((byte) 0x00, bytes[92]);
        assertEquals((byte) 0x01, bytes[93]);
        assertEquals((byte) 0x00, bytes[94]);
        assertEquals((byte) 0x00, bytes[95]);
    }

    // -----------------------------------------------------------------------
    // GiiDeviceCreation (no valuators)
    // -----------------------------------------------------------------------

    /**
     * Verifies the GiiDeviceCreation message byte format with no valuators (big-endian).
     */
    @Test
    void testGiiDeviceCreation_noValuators_bigEndian_byteFormat() throws IOException {
        GiiDeviceCreation msg = GiiDeviceCreation.newBuilder()
                .bigEndian(true)
                .deviceName("Test")
                .vendorId(0x1234L)
                .productId(0x5678L)
                .canGenerate(0L)
                .numRegisters(0L)
                .valuators(List.of())
                .numButtons(2L)
                .build();
        byte[] bytes = serialize(msg::write);

        // 1(type) + 1(endian+sub) + 2(length) + 32(name) + 6*4(fields) = 60 bytes
        assertEquals(60, bytes.length);
        assertEquals((byte) 253, bytes[0], "message-type = 253");
        assertEquals((byte) 0x82, bytes[1], "big-endian + sub-type 2 = 0x82");

        // length = 56 (payload without header) as big-endian U16
        assertEquals((byte) 0x00, bytes[2]);
        assertEquals((byte) 56, bytes[3]);

        // deviceName = "Test" at offset 4: T=0x54, e=0x65, s=0x73, t=0x74
        assertEquals((byte) 0x54, bytes[4], "'T'");
        assertEquals((byte) 0x65, bytes[5], "'e'");
        assertEquals((byte) 0x73, bytes[6], "'s'");
        assertEquals((byte) 0x74, bytes[7], "'t'");
        // remaining name bytes are NUL, last NUL terminator at offset 35
        assertEquals((byte) 0, bytes[34]);
        assertEquals((byte) 0, bytes[35], "NUL terminator of device-name");

        // vendorId = 0x1234 as big-endian EU32 at offset 36
        assertEquals((byte) 0x00, bytes[36]);
        assertEquals((byte) 0x00, bytes[37]);
        assertEquals((byte) 0x12, bytes[38]);
        assertEquals((byte) 0x34, bytes[39]);

        // productId = 0x5678 as big-endian EU32 at offset 40
        assertEquals((byte) 0x00, bytes[40]);
        assertEquals((byte) 0x00, bytes[41]);
        assertEquals((byte) 0x56, bytes[42]);
        assertEquals((byte) 0x78, bytes[43]);

        // canGenerate = 0 at offset 44
        assertEquals((byte) 0, bytes[44]);
        assertEquals((byte) 0, bytes[45]);
        assertEquals((byte) 0, bytes[46]);
        assertEquals((byte) 0, bytes[47]);

        // numRegisters = 0 at offset 48
        assertEquals((byte) 0, bytes[48]);
        assertEquals((byte) 0, bytes[49]);
        assertEquals((byte) 0, bytes[50]);
        assertEquals((byte) 0, bytes[51]);

        // numValuators = 0 at offset 52
        assertEquals((byte) 0, bytes[52]);
        assertEquals((byte) 0, bytes[53]);
        assertEquals((byte) 0, bytes[54]);
        assertEquals((byte) 0, bytes[55]);

        // numButtons = 2 at offset 56
        assertEquals((byte) 0, bytes[56]);
        assertEquals((byte) 0, bytes[57]);
        assertEquals((byte) 0, bytes[58]);
        assertEquals((byte) 2, bytes[59]);
    }

    // -----------------------------------------------------------------------
    // GiiDeviceCreation (with one valuator)
    // -----------------------------------------------------------------------

    /**
     * Verifies GiiDeviceCreation with one valuator has the correct size and
     * encodes the valuator at the correct offset.
     */
    @Test
    void testGiiDeviceCreation_oneValuator_bigEndian_byteFormat() throws IOException {
        GiiValuator val = GiiValuator.newBuilder()
                .index(0L)
                .longName("").shortName("")
                .rangeMin(0).rangeCenter(512).rangeMax(1023)
                .siUnit(0L).siAdd(0).siMul(1).siDiv(1).siShift(0)
                .build();
        GiiDeviceCreation msg = GiiDeviceCreation.newBuilder()
                .bigEndian(true)
                .deviceName("")
                .vendorId(0L).productId(0L).canGenerate(0L).numRegisters(0L)
                .valuators(List.of(val))
                .numButtons(0L)
                .build();
        byte[] bytes = serialize(msg::write);

        // 60 (base) + 116 (one valuator) = 176 bytes
        assertEquals(176, bytes.length);

        // length field = 56 + 116 = 172 as big-endian U16
        assertEquals((byte) 0x00, bytes[2]);
        assertEquals((byte) 172, bytes[3]);

        // numValuators at offset 52 = 1
        assertEquals((byte) 0, bytes[52]);
        assertEquals((byte) 0, bytes[53]);
        assertEquals((byte) 0, bytes[54]);
        assertEquals((byte) 1, bytes[55], "numValuators = 1");

        // Valuator starts at offset 60
        // index = 0
        assertEquals((byte) 0, bytes[60]);
        assertEquals((byte) 0, bytes[61]);
        assertEquals((byte) 0, bytes[62]);
        assertEquals((byte) 0, bytes[63]);

        // GiiValuator offsets within full byte array (valuator starts at 60):
        // index(4) + longName(74+1=75) + shortName(4+1=5) = 84 bytes before rangeMin
        // rangeMin at 60+84=144, rangeCenter at 60+88=148, rangeMax at 60+92=152

        // rangeCenter = 512 = 0x00000200 at offset 60+88=148
        assertEquals((byte) 0x00, bytes[148]);
        assertEquals((byte) 0x00, bytes[149]);
        assertEquals((byte) 0x02, bytes[150]);
        assertEquals((byte) 0x00, bytes[151]);

        // rangeMax = 1023 = 0x000003FF at offset 60+92=152
        assertEquals((byte) 0x00, bytes[152]);
        assertEquals((byte) 0x00, bytes[153]);
        assertEquals((byte) 0x03, bytes[154]);
        assertEquals((byte) 0xFF, bytes[155]);
    }
}
