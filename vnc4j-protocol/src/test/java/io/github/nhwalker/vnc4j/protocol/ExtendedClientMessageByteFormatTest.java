package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.messages.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying that optional/extended client-to-server messages conform to the
 * byte-level format specified in the RFB protocol specification (rfbproto.rst.txt).
 */
class ExtendedClientMessageByteFormatTest {

    @FunctionalInterface
    interface Writable {
        void write(java.io.OutputStream out) throws IOException;
    }

    private byte[] serialize(Writable w) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        w.write(baos);
        return baos.toByteArray();
    }

    // -----------------------------------------------------------------------
    // EnableContinuousUpdates (type 150)
    // -----------------------------------------------------------------------

    /**
     * Verifies the EnableContinuousUpdates message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - EnableContinuousUpdates:
     *
     *   This message informs the server to switch between only sending
     *   FramebufferUpdate messages as a result of a FramebufferUpdateRequest
     *   message, or sending FramebufferUpdate messages continuously.
     *
     *   =============== ==================== ========== =======================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== =======================
     *   1               U8                   150        message-type
     *   1               U8                              enable-flag
     *   2               U16                             x-position
     *   2               U16                             y-position
     *   2               U16                             width
     *   2               U16                             height
     *   =============== ==================== ========== =======================
     *
     *   If enable-flag is non-zero, the server can start sending FramebufferUpdate
     *   messages continuously for the area specified.
     * </pre>
     */
    @Test
    void testEnableContinuousUpdates_enable_byteFormat() throws IOException {
        EnableContinuousUpdates msg = EnableContinuousUpdates.newBuilder()
                .enable(true)
                .x(0).y(0).width(1920).height(1080)
                .build();
        byte[] bytes = serialize(msg::write);

        assertEquals(10, bytes.length, "EnableContinuousUpdates must be exactly 10 bytes");
        // byte 0: message-type = 150
        assertEquals((byte) 150, bytes[0], "message-type must be 150");
        // byte 1: enable-flag = 1 (non-zero = enabled)
        assertNotEquals((byte) 0, bytes[1], "enable=true must be non-zero");
        // bytes 2-3: x = 0
        assertEquals((byte) 0, bytes[2]);
        assertEquals((byte) 0, bytes[3]);
        // bytes 4-5: y = 0
        assertEquals((byte) 0, bytes[4]);
        assertEquals((byte) 0, bytes[5]);
        // bytes 6-7: width = 1920 (0x07 0x80)
        assertEquals((byte) 0x07, bytes[6]);
        assertEquals((byte) 0x80, bytes[7]);
        // bytes 8-9: height = 1080 (0x04 0x38)
        assertEquals((byte) 0x04, bytes[8]);
        assertEquals((byte) 0x38, bytes[9]);
    }

    /**
     * Verifies EnableContinuousUpdates with enable=false (zero byte).
     *
     * <pre>
     * From rfbproto.rst.txt - EnableContinuousUpdates:
     *
     *   If enable-flag is zero, the server must only send FramebufferUpdate messages
     *   as a result of receiving FramebufferUpdateRequest messages.
     * </pre>
     */
    @Test
    void testEnableContinuousUpdates_disable_byteFormat() throws IOException {
        EnableContinuousUpdates msg = EnableContinuousUpdates.newBuilder()
                .enable(false)
                .x(0).y(0).width(0).height(0)
                .build();
        byte[] bytes = serialize(msg::write);

        assertEquals(10, bytes.length);
        assertEquals((byte) 150, bytes[0]);
        assertEquals((byte) 0, bytes[1], "enable=false must be zero");
    }

    // -----------------------------------------------------------------------
    // ClientFence (type 248)
    // -----------------------------------------------------------------------

    /**
     * Verifies the ClientFence message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - ClientFence:
     *
     *   A client supporting the Fence extension sends this to request a
     *   synchronisation of the data stream.
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
     *
     *   Flag bits:
     *   Bit 0   BlockBefore
     *   Bit 1   BlockAfter
     *   Bit 2   SyncNext
     *   Bit 31  Request (new request expecting a response)
     * </pre>
     */
    @Test
    void testClientFence_withPayload_byteFormat() throws IOException {
        // flags: Request bit (bit 31) = 0x80000001 (Request | BlockBefore)
        int flags = 0x80000001;
        byte[] payload = new byte[]{0x01, 0x02, 0x03};
        ClientFence msg = ClientFence.newBuilder()
                .flags(flags)
                .payload(payload)
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 1 + 3 + 4 + 1 + 3 = 12 bytes
        assertEquals(12, bytes.length);

        // byte 0: message-type = 248
        assertEquals((byte) 248, bytes[0], "message-type must be 248 for ClientFence");
        // bytes 1-3: padding
        assertEquals((byte) 0, bytes[1]);
        assertEquals((byte) 0, bytes[2]);
        assertEquals((byte) 0, bytes[3]);
        // bytes 4-7: flags = 0x80000001 (big-endian)
        assertEquals((byte) 0x80, bytes[4]);
        assertEquals((byte) 0x00, bytes[5]);
        assertEquals((byte) 0x00, bytes[6]);
        assertEquals((byte) 0x01, bytes[7]);
        // byte 8: payload length = 3
        assertEquals((byte) 3, bytes[8]);
        // bytes 9-11: payload
        assertEquals((byte) 0x01, bytes[9]);
        assertEquals((byte) 0x02, bytes[10]);
        assertEquals((byte) 0x03, bytes[11]);
    }

    /**
     * Verifies ClientFence with empty payload.
     */
    @Test
    void testClientFence_emptyPayload_byteFormat() throws IOException {
        ClientFence msg = ClientFence.newBuilder()
                .flags(0)
                .payload(new byte[0])
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 1 + 3 + 4 + 1 = 9 bytes
        assertEquals(9, bytes.length);
        assertEquals((byte) 248, bytes[0]);
        assertEquals((byte) 0, bytes[8], "payload length must be 0");
    }

    // -----------------------------------------------------------------------
    // XvpClientMessage (type 250)
    // -----------------------------------------------------------------------

    /**
     * Verifies the xvp Client Message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - xvp Client Message:
     *
     *   A client supporting the xvp extension sends this to request that the server
     *   initiate a clean shutdown, clean reboot or abrupt reset of the system.
     *
     *   =============== ==================== ========== =======================
     *   No. of bytes    Type                 [Value]    Description
     *   =============== ==================== ========== =======================
     *   1               U8                   250        message-type
     *   1                                               padding
     *   1               U8                   1          xvp-extension-version
     *   1               U8                              xvp-message-code
     *   =============== ==================== ========== =======================
     *
     *   The possible values for xvp-message-code are:
     *   2 - XVP_SHUTDOWN
     *   3 - XVP_REBOOT
     *   4 - XVP_RESET
     * </pre>
     */
    @Test
    void testXvpClientMessage_shutdown_byteFormat() throws IOException {
        XvpClientMessage msg = XvpClientMessage.newBuilder()
                .xvpVersion(1)
                .xvpMessageCode(2) // XVP_SHUTDOWN
                .build();
        byte[] bytes = serialize(msg::write);

        assertEquals(4, bytes.length, "XvpClientMessage must be exactly 4 bytes");
        assertEquals((byte) 250, bytes[0], "message-type must be 250");
        assertEquals((byte) 0, bytes[1], "padding must be 0");
        assertEquals((byte) 1, bytes[2], "xvp-extension-version must be 1");
        assertEquals((byte) 2, bytes[3], "xvp-message-code must be 2 (XVP_SHUTDOWN)");
    }

    /**
     * Verifies XvpClientMessage for reboot and reset codes.
     */
    @Test
    void testXvpClientMessage_reboot_byteFormat() throws IOException {
        XvpClientMessage msg = XvpClientMessage.newBuilder()
                .xvpVersion(1)
                .xvpMessageCode(3) // XVP_REBOOT
                .build();
        byte[] bytes = serialize(msg::write);

        assertEquals(4, bytes.length);
        assertEquals((byte) 250, bytes[0]);
        assertEquals((byte) 0, bytes[1]);
        assertEquals((byte) 1, bytes[2]);
        assertEquals((byte) 3, bytes[3], "xvp-message-code must be 3 (XVP_REBOOT)");
    }

    // -----------------------------------------------------------------------
    // SetDesktopSize (type 251)
    // -----------------------------------------------------------------------

    /**
     * Verifies the SetDesktopSize message byte format.
     *
     * <pre>
     * From rfbproto.rst.txt - SetDesktopSize:
     *
     *   Requests a change of desktop size.
     *
     *   ======================== ================= ======= ====================
     *   No. of bytes             Type              [Value] Description
     *   ======================== ================= ======= ====================
     *   1                        U8                251     message-type
     *   1                                                  padding
     *   2                        U16                       width
     *   2                        U16                       height
     *   1                        U8                        number-of-screens
     *   1                                                  padding
     *   number-of-screens * 16   SCREEN array              screens
     *   ======================== ================= ======= ====================
     *
     *   where SCREEN is:
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
    void testSetDesktopSize_noScreens_byteFormat() throws IOException {
        SetDesktopSize msg = SetDesktopSize.newBuilder()
                .width(1280)
                .height(720)
                .screens(List.of())
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 1 + 1 + 2 + 2 + 1 + 1 = 8 bytes
        assertEquals(8, bytes.length, "SetDesktopSize header must be 8 bytes with no screens");
        assertEquals((byte) 251, bytes[0], "message-type must be 251");
        assertEquals((byte) 0, bytes[1], "padding must be 0");
        // width = 1280 (0x05 0x00)
        assertEquals((byte) 0x05, bytes[2]);
        assertEquals((byte) 0x00, bytes[3]);
        // height = 720 (0x02 0xD0)
        assertEquals((byte) 0x02, bytes[4]);
        assertEquals((byte) 0xD0, bytes[5]);
        // number-of-screens = 0
        assertEquals((byte) 0, bytes[6]);
        // padding
        assertEquals((byte) 0, bytes[7]);
    }

    /**
     * Verifies SetDesktopSize with one screen (16 bytes per Screen).
     */
    @Test
    void testSetDesktopSize_withScreen_byteFormat() throws IOException {
        Screen screen = Screen.newBuilder()
                .id(1L)
                .x(0).y(0)
                .width(1280).height(720)
                .flags(0L)
                .build();
        SetDesktopSize msg = SetDesktopSize.newBuilder()
                .width(1280).height(720)
                .screens(List.of(screen))
                .build();
        byte[] bytes = serialize(msg::write);

        // Total: 8 + 16 = 24 bytes
        assertEquals(24, bytes.length);
        assertEquals((byte) 251, bytes[0]);
        assertEquals((byte) 1, bytes[6], "number-of-screens must be 1");

        // Screen starts at offset 8
        // id = 1 (big-endian U32)
        assertEquals((byte) 0, bytes[8]);
        assertEquals((byte) 0, bytes[9]);
        assertEquals((byte) 0, bytes[10]);
        assertEquals((byte) 1, bytes[11]);
        // x = 0
        assertEquals((byte) 0, bytes[12]);
        assertEquals((byte) 0, bytes[13]);
        // y = 0
        assertEquals((byte) 0, bytes[14]);
        assertEquals((byte) 0, bytes[15]);
        // width = 1280 (0x05 0x00)
        assertEquals((byte) 0x05, bytes[16]);
        assertEquals((byte) 0x00, bytes[17]);
        // height = 720 (0x02 0xD0)
        assertEquals((byte) 0x02, bytes[18]);
        assertEquals((byte) 0xD0, bytes[19]);
        // flags = 0
        assertEquals((byte) 0, bytes[20]);
        assertEquals((byte) 0, bytes[21]);
        assertEquals((byte) 0, bytes[22]);
        assertEquals((byte) 0, bytes[23]);
    }

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
