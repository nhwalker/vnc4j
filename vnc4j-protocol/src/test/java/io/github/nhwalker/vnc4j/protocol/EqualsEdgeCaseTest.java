package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.messages.*;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for equals() edge cases: same-object identity (this == o → true) and
 * wrong-type argument (o instanceof X → false). These cover the 2-instruction
 * bodies of the two early-return guards present in every Impl.equals() method.
 *
 * <p>Each test follows the pattern:
 * <pre>
 * assertTrue(obj.equals(obj));         // covers "this == o → return true"
 * assertFalse(obj.equals(new Object())); // covers "!(o instanceof T) → return false"
 * </pre>
 */
class EqualsEdgeCaseTest {

    private static final PixelFormat PF = PixelFormat.newBuilder()
            .bitsPerPixel(32).depth(24).bigEndian(false).trueColour(true)
            .redMax(255).greenMax(255).blueMax(255)
            .redShift(16).greenShift(8).blueShift(0).build();

    // Helper to test the two equals() guard branches for any protocol object
    private static void assertEqualsGuards(Object obj) {
        assertTrue(obj.equals(obj));
        assertFalse(obj.equals(new Object()));
        assertFalse(obj.equals(null));
    }

    @Test
    void testClientCutText_equalsGuards() {
        assertEqualsGuards(ClientCutText.newBuilder().text(new byte[]{1}).build());
    }

    @Test
    void testServerCutText_equalsGuards() {
        assertEqualsGuards(ServerCutText.newBuilder().text(new byte[]{1}).build());
    }

    @Test
    void testRfbRectangleLastRect_equalsGuards() {
        assertEqualsGuards(RfbRectangleLastRect.newBuilder().x(0).y(0).width(0).height(0).build());
    }

    @Test
    void testRfbRectangleDesktopSize_equalsGuards() {
        assertEqualsGuards(RfbRectangleDesktopSize.newBuilder().x(0).y(0).width(100).height(100).build());
    }

    @Test
    void testRfbRectangleJpeg_equalsGuards() {
        assertEqualsGuards(RfbRectangleJpeg.newBuilder().x(0).y(0).width(4).height(4).data(new byte[]{(byte)0xFF,(byte)0xD8}).build());
    }

    @Test
    void testRfbRectangleTightPngJpeg_equalsGuards() {
        assertEqualsGuards(RfbRectangleTightPngJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0)
                .jpegData(new byte[]{1}).build());
    }

    @Test
    void testRfbRectangleTightPngPng_equalsGuards() {
        assertEqualsGuards(RfbRectangleTightPngPng.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0)
                .pngData(new byte[]{(byte)0x89}).build());
    }

    @Test
    void testRfbRectangleExtendedDesktopSize_equalsGuards() {
        assertEqualsGuards(RfbRectangleExtendedDesktopSize.newBuilder()
                .x(0).y(0).width(100).height(100).screens(List.of()).build());
    }

    @Test
    void testRfbRectangleRaw_equalsGuards() {
        assertEqualsGuards(RfbRectangleRaw.newBuilder().x(0).y(0).width(1).height(1).pixels(new byte[]{0}).build());
    }

    @Test
    void testRfbRectangleRre_equalsGuards() {
        assertEqualsGuards(RfbRectangleRre.newBuilder()
                .x(0).y(0).width(1).height(1)
                .background(new byte[]{0}).subrects(List.of()).build());
    }

    @Test
    void testRfbRectangleCopyRect_equalsGuards() {
        assertEqualsGuards(RfbRectangleCopyRect.newBuilder()
                .x(0).y(0).width(1).height(1).srcX(0).srcY(0).build());
    }

    @Test
    void testRfbRectangleCursor_equalsGuards() {
        assertEqualsGuards(RfbRectangleCursor.newBuilder()
                .x(0).y(0).width(1).height(1)
                .pixels(new byte[4]).bitmask(new byte[1]).build());
    }

    @Test
    void testRfbRectangleTightFill_equalsGuards() {
        assertEqualsGuards(RfbRectangleTightFill.newBuilder()
                .x(0).y(0).width(1).height(1).streamResets(0).fillColor(new byte[]{0}).build());
    }

    @Test
    void testRfbRectangleTightJpeg_equalsGuards() {
        assertEqualsGuards(RfbRectangleTightJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0).jpegData(new byte[]{1}).build());
    }

    @Test
    void testRfbRectangleTightPngFill_equalsGuards() {
        assertEqualsGuards(RfbRectangleTightPngFill.newBuilder()
                .x(0).y(0).width(1).height(1).streamResets(0).fillColor(new byte[]{0}).build());
    }

    @Test
    void testRfbRectangleZlib_equalsGuards() {
        assertEqualsGuards(RfbRectangleZlib.newBuilder()
                .x(0).y(0).width(1).height(1).zlibData(new byte[]{1}).build());
    }

    @Test
    void testRfbRectangleZrle_equalsGuards() {
        assertEqualsGuards(RfbRectangleZrle.newBuilder()
                .x(0).y(0).width(1).height(1).zlibData(new byte[]{1}).build());
    }

    @Test
    void testRfbRectangleHextile_equalsGuards() {
        assertEqualsGuards(RfbRectangleHextile.newBuilder()
                .x(0).y(0).width(16).height(16).tiles(List.of()).build());
    }

    @Test
    void testHextileTile_equalsGuards() {
        HextileTile tile = HextileTile.newBuilder()
                .subencoding(HextileTile.SUBENC_BACKGROUND_SPECIFIED)
                .background(new byte[]{0x11}).build();
        assertEqualsGuards(tile);
    }

    @Test
    void testRreSubrect_equalsGuards() {
        assertEqualsGuards(RreSubrect.newBuilder()
                .pixel(new byte[]{0}).x(0).y(0).width(1).height(1).build());
    }

    @Test
    void testHextileSubrect_equalsGuards() {
        assertEqualsGuards(HextileSubrect.newBuilder()
                .pixel(null).x(0).y(0).width(1).height(1).build());
    }

    @Test
    void testClientFence_equalsGuards() {
        assertEqualsGuards(ClientFence.newBuilder().flags(1).payload(new byte[]{1}).build());
    }

    @Test
    void testServerFence_equalsGuards() {
        assertEqualsGuards(ServerFence.newBuilder().flags(1).payload(new byte[]{1}).build());
    }

    @Test
    void testKeyEvent_equalsGuards() {
        assertEqualsGuards(KeyEvent.newBuilder().down(true).key(0x41).build());
    }

    @Test
    void testFramebufferUpdate_equalsGuards() {
        assertEqualsGuards(FramebufferUpdate.newBuilder().rectangles(List.of()).build());
    }

    @Test
    void testSetEncodings_equalsGuards() {
        assertEqualsGuards(SetEncodings.newBuilder().encodings(List.of(0)).build());
    }

    @Test
    void testEnableContinuousUpdates_equalsGuards() {
        assertEqualsGuards(EnableContinuousUpdates.newBuilder()
                .enable(true).x(0).y(0).width(800).height(600).build());
    }

    @Test
    void testSecurityTypes_equalsGuards() {
        assertEqualsGuards(SecurityTypes.newBuilder().securityTypes(List.of(1)).build());
    }

    @Test
    void testServerInit_equalsGuards() {
        assertEqualsGuards(ServerInit.newBuilder()
                .framebufferWidth(800).framebufferHeight(600).pixelFormat(PF).name("Test").build());
    }

    @Test
    void testSetColourMapEntries_equalsGuards() {
        ColourMapEntry entry = ColourMapEntry.newBuilder().red(0xFFFF).green(0).blue(0).build();
        assertEqualsGuards(SetColourMapEntries.newBuilder()
                .firstColour(0).colours(List.of(entry)).build());
    }

    @Test
    void testSetDesktopSize_equalsGuards() {
        Screen screen = Screen.newBuilder().id(1).x(0).y(0).width(1920).height(1080).flags(0).build();
        assertEqualsGuards(SetDesktopSize.newBuilder()
                .width(1920).height(1080).screens(List.of(screen)).build());
    }

    @Test
    void testRfbRectangleTightBasic_equalsGuards() {
        RfbRectangleTightBasic orig = RfbRectangleTightBasic.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0)
                .filterType(RfbRectangleTightBasic.FILTER_COPY)
                .paletteSize(0).palette(new byte[0])
                .compressedData(new byte[0])
                .build();
        assertEqualsGuards(orig);
    }

}
