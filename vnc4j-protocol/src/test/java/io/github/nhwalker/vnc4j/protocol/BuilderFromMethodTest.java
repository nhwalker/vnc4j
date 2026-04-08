package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.messages.*;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that exercise the {@code Builder.from()} default methods of interface
 * builders that are not yet covered. Each test calls {@code from()} to copy
 * an instance via the builder and verifies the copy equals the original.
 */
class BuilderFromMethodTest {

    private static final PixelFormat PF = PixelFormat.newBuilder()
            .bitsPerPixel(32).depth(24).bigEndian(false).trueColour(true)
            .redMax(255).greenMax(255).blueMax(255)
            .redShift(16).greenShift(8).blueShift(0).build();

    @Test
    void testClientInit_from() {
        ClientInit orig = ClientInit.newBuilder().shared(true).build();
        ClientInit copy = ClientInit.newBuilder().from(orig).build();
        assertEquals(orig.shared(), copy.shared());
    }

    @Test
    void testProtocolVersion_from() {
        ProtocolVersion orig = ProtocolVersion.newBuilder().major(3).minor(8).build();
        ProtocolVersion copy = ProtocolVersion.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testSecurityTypeSelection_from() {
        SecurityTypeSelection orig = SecurityTypeSelection.newBuilder().securityType(1).build();
        SecurityTypeSelection copy = SecurityTypeSelection.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testSecurityTypes_from() {
        SecurityTypes orig = SecurityTypes.newBuilder().securityTypes(List.of(1, 2)).build();
        SecurityTypes copy = SecurityTypes.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testFramebufferUpdate_from() {
        RfbRectangleDesktopSize rect = RfbRectangleDesktopSize.newBuilder()
                .x(0).y(0).width(800).height(600).build();
        FramebufferUpdate orig = FramebufferUpdate.newBuilder()
                .rectangles(List.of(rect)).build();
        FramebufferUpdate copy = FramebufferUpdate.newBuilder().from(orig).build();
        assertEquals(orig.rectangles().size(), copy.rectangles().size());
    }

    @Test
    void testFramebufferUpdateRequest_from() {
        FramebufferUpdateRequest orig = FramebufferUpdateRequest.newBuilder()
                .incremental(true).x(0).y(0).width(800).height(600).build();
        FramebufferUpdateRequest copy = FramebufferUpdateRequest.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testKeyEvent_from() {
        KeyEvent orig = KeyEvent.newBuilder().down(true).key(0x41).build();
        KeyEvent copy = KeyEvent.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testPointerEvent_from() {
        PointerEvent orig = PointerEvent.newBuilder().buttonMask(3).x(100).y(200).build();
        PointerEvent copy = PointerEvent.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testSetColourMapEntries_from() {
        ColourMapEntry entry = ColourMapEntry.newBuilder().red(0xFFFF).green(0).blue(0).build();
        SetColourMapEntries orig = SetColourMapEntries.newBuilder()
                .firstColour(0).colours(List.of(entry)).build();
        SetColourMapEntries copy = SetColourMapEntries.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testRfbRectangleRre_from() {
        RreSubrect sr = RreSubrect.newBuilder().pixel(new byte[]{1}).x(0).y(0).width(2).height(2).build();
        RfbRectangleRre orig = RfbRectangleRre.newBuilder()
                .x(0).y(0).width(4).height(4)
                .background(new byte[]{0}).subrects(List.of(sr)).build();
        RfbRectangleRre copy = RfbRectangleRre.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testRfbRectangleLastRect_from() {
        RfbRectangleLastRect orig = RfbRectangleLastRect.newBuilder()
                .x(0).y(0).width(0).height(0).build();
        RfbRectangleLastRect copy = RfbRectangleLastRect.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testRfbRectangleExtendedDesktopSize_from() {
        Screen screen = Screen.newBuilder().id(1).x(0).y(0).width(1920).height(1080).flags(0).build();
        RfbRectangleExtendedDesktopSize orig = RfbRectangleExtendedDesktopSize.newBuilder()
                .x(0).y(0).width(1920).height(1080).screens(List.of(screen)).build();
        RfbRectangleExtendedDesktopSize copy = RfbRectangleExtendedDesktopSize.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testRfbRectangleJpeg_from() {
        RfbRectangleJpeg orig = RfbRectangleJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).data(new byte[]{(byte)0xFF, (byte)0xD8}).build();
        RfbRectangleJpeg copy = RfbRectangleJpeg.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testRfbRectangleTightJpeg_from() {
        RfbRectangleTightJpeg orig = RfbRectangleTightJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0)
                .jpegData(new byte[]{(byte)0xFF, (byte)0xD8}).build();
        RfbRectangleTightJpeg copy = RfbRectangleTightJpeg.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testRfbRectangleTightPngJpeg_from() {
        RfbRectangleTightPngJpeg orig = RfbRectangleTightPngJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0)
                .jpegData(new byte[]{0x01, 0x02}).build();
        RfbRectangleTightPngJpeg copy = RfbRectangleTightPngJpeg.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testRfbRectangleTightPngPng_from() {
        RfbRectangleTightPngPng orig = RfbRectangleTightPngPng.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0)
                .pngData(new byte[]{(byte)0x89, 0x50}).build();
        RfbRectangleTightPngPng copy = RfbRectangleTightPngPng.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testRfbRectangleZlib_from() {
        RfbRectangleZlib orig = RfbRectangleZlib.newBuilder()
                .x(0).y(0).width(4).height(4).zlibData(new byte[]{1, 2}).build();
        RfbRectangleZlib copy = RfbRectangleZlib.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testRfbRectangleZrle_from() {
        RfbRectangleZrle orig = RfbRectangleZrle.newBuilder()
                .x(0).y(0).width(4).height(4).zlibData(new byte[]{3, 4}).build();
        RfbRectangleZrle copy = RfbRectangleZrle.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }
}
