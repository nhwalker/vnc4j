package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.messages.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for remaining coverage gaps: from() builders for SetEncodings, SetPixelFormat;
 * RRE read round-trips; and VncServer default method coverage via anonymous implementations.
 */
class RemainingCoverageTest {

    private static final PixelFormat PF_8BPP = PixelFormat.newBuilder()
            .bitsPerPixel(8).depth(8).bigEndian(false).trueColour(false)
            .redMax(7).greenMax(7).blueMax(3)
            .redShift(5).greenShift(2).blueShift(0).build();

    private static final PixelFormat PF_32BPP = PixelFormat.newBuilder()
            .bitsPerPixel(32).depth(24).bigEndian(false).trueColour(true)
            .redMax(255).greenMax(255).blueMax(255)
            .redShift(16).greenShift(8).blueShift(0).build();

    @FunctionalInterface
    interface Writable {
        void write(java.io.OutputStream out) throws IOException;
    }

    private byte[] serialize(Writable w) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        w.write(baos);
        return baos.toByteArray();
    }

    private InputStream streamOf(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    // -----------------------------------------------------------------------
    // Missing from() builder tests
    // -----------------------------------------------------------------------

    @Test
    void testSetEncodings_from() {
        SetEncodings orig = SetEncodings.newBuilder().encodings(List.of(0, 1, 5)).build();
        SetEncodings copy = SetEncodings.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testSetPixelFormat_from() {
        SetPixelFormat orig = SetPixelFormat.newBuilder().pixelFormat(PF_32BPP).build();
        SetPixelFormat copy = SetPixelFormat.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    // -----------------------------------------------------------------------
    // RRE rectangle read round-trip via RfbRectangle.read()
    // -----------------------------------------------------------------------

    /**
     * RRE (encoding type 2) wire format after header:
     * <pre>
     * numberOfSubrects : U32
     * background       : bytesPerPixel bytes
     * subrects         : numberOfSubrects × (pixel + x:U16 + y:U16 + w:U16 + h:U16)
     * </pre>
     */
    @Test
    void testRfbRectangleRre_readRoundTrip() throws IOException {
        RreSubrect sr = RreSubrect.newBuilder()
                .pixel(new byte[]{0x55}).x(1).y(2).width(3).height(4).build();
        RfbRectangleRre orig = RfbRectangleRre.newBuilder()
                .x(0).y(0).width(8).height(8)
                .background(new byte[]{0x00}).subrects(List.of(sr)).build();
        byte[] bytes = serialize(orig::write);
        RfbRectangle result = RfbRectangle.read(streamOf(bytes), PF_8BPP);
        assertInstanceOf(RfbRectangleRre.class, result);
        RfbRectangleRre copy = (RfbRectangleRre) result;
        assertEquals(orig, copy);
        assertArrayEquals(new byte[]{0x00}, copy.background());
        assertEquals(1, copy.subrects().size());
        assertEquals(sr, copy.subrects().get(0));
    }

    @Test
    void testRfbRectangleRre_fromBuilder() {
        RreSubrect sr = RreSubrect.newBuilder().pixel(new byte[]{0x11}).x(0).y(0).width(2).height(2).build();
        RfbRectangleRre orig = RfbRectangleRre.newBuilder()
                .x(1).y(2).width(4).height(4)
                .background(new byte[]{0x00}).subrects(List.of(sr)).build();
        RfbRectangleRre copy = RfbRectangleRre.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    // -----------------------------------------------------------------------
    // TightPngJpeg and TightPngPng read round-trips (previously only tested write)
    // -----------------------------------------------------------------------

    @Test
    void testRfbRectangleTightPngJpeg_readRoundTrip() throws IOException {
        RfbRectangleTightPngJpeg orig = RfbRectangleTightPngJpeg.newBuilder()
                .x(0).y(0).width(4).height(4)
                .streamResets(0).jpegData(new byte[]{(byte)0xFF, (byte)0xD8, 0x01}).build();
        byte[] bytes = serialize(orig::write);
        RfbRectangle result = RfbRectangle.read(streamOf(bytes), PF_32BPP);
        assertInstanceOf(RfbRectangleTightPngJpeg.class, result);
        assertArrayEquals(new byte[]{(byte)0xFF, (byte)0xD8, 0x01},
                ((RfbRectangleTightPngJpeg) result).jpegData());
    }

    @Test
    void testRfbRectangleTightPngPng_readRoundTrip() throws IOException {
        RfbRectangleTightPngPng orig = RfbRectangleTightPngPng.newBuilder()
                .x(0).y(0).width(4).height(4)
                .streamResets(0).pngData(new byte[]{(byte)0x89, 0x50, 0x4E, 0x47}).build();
        byte[] bytes = serialize(orig::write);
        RfbRectangle result = RfbRectangle.read(streamOf(bytes), PF_32BPP);
        assertInstanceOf(RfbRectangleTightPngPng.class, result);
        assertArrayEquals(new byte[]{(byte)0x89, 0x50, 0x4E, 0x47},
                ((RfbRectangleTightPngPng) result).pngData());
    }

    // -----------------------------------------------------------------------
    // VncServer default methods
    // -----------------------------------------------------------------------

    /**
     * VncServer default methods (onSetPixelFormat, onSetEncodings, onKeyEvent,
     * onPointerEvent, onClientCutText, onClose) do nothing by default.
     * Test that calling them on an anonymous implementation works.
     */
    @Test
    void testVncServer_defaultMethods_doNothing() {
        VncServer server = new VncServer() {
            @Override
            public ServerInit onClientInit(ClientInit clientInit) {
                return ServerInit.newBuilder()
                        .framebufferWidth(1).framebufferHeight(1)
                        .pixelFormat(PF_32BPP).name("Test").build();
            }
            @Override
            public void onFramebufferUpdateRequest(FramebufferUpdateRequest msg) {}
        };

        // All default methods should execute without exception
        assertDoesNotThrow(() -> server.onSetPixelFormat(
                SetPixelFormat.newBuilder().pixelFormat(PF_32BPP).build()));
        assertDoesNotThrow(() -> server.onSetEncodings(
                SetEncodings.newBuilder().encodings(List.of(0)).build()));
        assertDoesNotThrow(() -> server.onKeyEvent(
                KeyEvent.newBuilder().down(true).key(0x41).build()));
        assertDoesNotThrow(() -> server.onPointerEvent(
                PointerEvent.newBuilder().buttonMask(0).x(0).y(0).build()));
        assertDoesNotThrow(() -> server.onClientCutText(
                ClientCutText.newBuilder().text(new byte[0]).build()));
        assertDoesNotThrow(() -> server.onClose());
    }

    /**
     * VncClient default methods (onSetColourMapEntries, onBell, onServerCutText, onClose,
     * clientInit) do nothing or return a default value.
     */
    @Test
    void testVncClient_defaultMethods() {
        VncClient client = new VncClient() {
            @Override public void onServerInit(ServerInit si) {}
            @Override public void onFramebufferUpdate(FramebufferUpdate msg) {}
        };

        assertDoesNotThrow(() -> client.onSetColourMapEntries(
                SetColourMapEntries.newBuilder().firstColour(0).colours(List.of()).build()));
        assertDoesNotThrow(() -> client.onBell(Bell.newBuilder().build()));
        assertDoesNotThrow(() -> client.onServerCutText(
                ServerCutText.newBuilder().text(new byte[0]).build()));
        assertDoesNotThrow(() -> client.onClose());
        // clientInit() returns shared=true by default
        ClientInit init = client.clientInit();
        assertTrue(init.shared());
    }

    // -----------------------------------------------------------------------
    // SecurityTypes edge case: empty list (zero-length)
    // -----------------------------------------------------------------------

    /**
     * When count=0, the RFB protocol sends a failure-reason (U32 length + bytes).
     * SecurityTypes.read() handles this by reading and discarding the failure reason.
     */
    @Test
    void testSecurityTypes_zeroCount_withFailureReason() throws IOException {
        // Manually construct the zero-count security types message with failure reason
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);
        dos.writeByte(0); // count = 0
        byte[] reason = "No security".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        dos.writeInt(reason.length);
        dos.write(reason);
        SecurityTypes copy = SecurityTypes.read(streamOf(baos.toByteArray()));
        assertEquals(List.of(), copy.securityTypes());
    }

    // -----------------------------------------------------------------------
    // HextileTile with coloured subrects
    // -----------------------------------------------------------------------

    @Test
    void testHextileTile_readRoundTrip_colouredSubrects() throws IOException {
        HextileSubrect sr = HextileSubrect.newBuilder()
                .pixel(new byte[]{0x33}).x(0).y(0).width(4).height(4).build();
        int subenc = HextileTile.SUBENC_BACKGROUND_SPECIFIED
                | HextileTile.SUBENC_ANY_SUBRECTS
                | HextileTile.SUBENC_SUBRECTS_COLOURED;
        HextileTile orig = HextileTile.newBuilder()
                .subencoding(subenc)
                .background(new byte[]{0x00})
                .subrects(List.of(sr)).build();
        byte[] bytes = serialize(orig::write);
        HextileTile copy = HextileTile.read(streamOf(bytes), 8, 8, 1);
        assertEquals(subenc, copy.subencoding());
        assertEquals(1, copy.subrects().size());
        assertArrayEquals(new byte[]{0x33}, copy.subrects().get(0).pixel());
    }

    @Test
    void testHextileTile_readRoundTrip_rawSubenc() throws IOException {
        // Raw hextile tile: subencoding=1, pixel data follows (tileW * tileH * bpp bytes)
        byte[] pixels = new byte[4]; // 2x2 tile, 1bpp
        pixels[0] = 0x11; pixels[1] = 0x22; pixels[2] = 0x33; pixels[3] = 0x44;
        HextileTile orig = HextileTile.newBuilder()
                .subencoding(HextileTile.SUBENC_RAW)
                .rawPixels(pixels).build();
        byte[] bytes = serialize(orig::write);
        HextileTile copy = HextileTile.read(streamOf(bytes), 2, 2, 1);
        assertEquals(HextileTile.SUBENC_RAW, copy.subencoding());
        assertArrayEquals(pixels, copy.rawPixels());
    }
}
