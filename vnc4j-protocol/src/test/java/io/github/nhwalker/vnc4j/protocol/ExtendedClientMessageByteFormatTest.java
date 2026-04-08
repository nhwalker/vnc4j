package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.messages.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying the byte-level format of supporting data structures
 * used in client-to-server messages.
 */
class ExtendedClientMessageByteFormatTest {

    // -----------------------------------------------------------------------
    // Screen (used in RfbRectangleExtendedDesktopSize)
    // -----------------------------------------------------------------------

    /**
     * Verifies the Screen structure is exactly 16 bytes.
     *
     * <pre>
     * From rfbproto.rst.txt - SetDesktopSize:
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
    void testScreen_byteFormat() throws IOException {
        Screen screen = Screen.newBuilder()
                .id(0xDEADBEEFL)
                .x(100).y(200)
                .width(800).height(600)
                .flags(0x00000001L)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        screen.write(baos);
        byte[] bytes = baos.toByteArray();

        assertEquals(16, bytes.length, "Screen must be exactly 16 bytes");
        // id = 0xDEADBEEF (big-endian U32)
        assertEquals((byte) 0xDE, bytes[0]);
        assertEquals((byte) 0xAD, bytes[1]);
        assertEquals((byte) 0xBE, bytes[2]);
        assertEquals((byte) 0xEF, bytes[3]);
        // x = 100 (0x00 0x64)
        assertEquals((byte) 0x00, bytes[4]);
        assertEquals((byte) 0x64, bytes[5]);
        // y = 200 (0x00 0xC8)
        assertEquals((byte) 0x00, bytes[6]);
        assertEquals((byte) 0xC8, bytes[7]);
        // width = 800 (0x03 0x20)
        assertEquals((byte) 0x03, bytes[8]);
        assertEquals((byte) 0x20, bytes[9]);
        // height = 600 (0x02 0x58)
        assertEquals((byte) 0x02, bytes[10]);
        assertEquals((byte) 0x58, bytes[11]);
        // flags = 1 (big-endian U32)
        assertEquals((byte) 0, bytes[12]);
        assertEquals((byte) 0, bytes[13]);
        assertEquals((byte) 0, bytes[14]);
        assertEquals((byte) 1, bytes[15]);
    }

}
