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
 * Tests for equals/hashCode/toString and read() round-trips for various types
 * that are not adequately covered by the byte-format tests alone.
 *
 * <p>Covers: RreSubrect, HextileSubrect, HextileTile, SecurityTypes, and various RfbRectangle types.
 */
class EqualityAndReadTest {

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
    // RreSubrect
    // -----------------------------------------------------------------------

    /**
     * RreSubrect wire format per rfbproto.rst.txt:
     * <pre>
     * pixel-value  : bytesPerPixel
     * x            : U16
     * y            : U16
     * width        : U16
     * height       : U16
     * </pre>
     */
    @Test
    void testRreSubrect_readRoundTrip() throws IOException {
        RreSubrect original = RreSubrect.newBuilder()
                .pixel(new byte[]{0x42}).x(3).y(7).width(10).height(5).build();
        byte[] bytes = serialize(original::write);
        RreSubrect copy = RreSubrect.read(streamOf(bytes), 1);
        assertEquals(original, copy);
        assertEquals(original.hashCode(), copy.hashCode());
    }

    @Test
    void testRreSubrect_equals_differentX() {
        RreSubrect a = RreSubrect.newBuilder().pixel(new byte[]{1}).x(1).y(0).width(2).height(2).build();
        RreSubrect b = RreSubrect.newBuilder().pixel(new byte[]{1}).x(2).y(0).width(2).height(2).build();
        assertNotEquals(a, b);
    }

    @Test
    void testRreSubrect_toString() {
        RreSubrect s = RreSubrect.newBuilder().pixel(new byte[]{0x11}).x(0).y(0).width(4).height(3).build();
        String str = s.toString();
        assertTrue(str.contains("RreSubrect") || str.length() > 0);
    }

    @Test
    void testRreSubrect_fromBuilder() {
        RreSubrect original = RreSubrect.newBuilder().pixel(new byte[]{0x55}).x(1).y(2).width(3).height(4).build();
        RreSubrect copy = RreSubrect.newBuilder().from(original).build();
        assertEquals(original, copy);
    }

    // -----------------------------------------------------------------------
    // HextileSubrect
    // -----------------------------------------------------------------------

    /**
     * HextileSubrect without SubrectsColoured: just xy-byte + wh-byte (2 bytes).
     * With SubrectsColoured: pixel bytes + xy-byte + wh-byte.
     * xy-byte: x in high nibble, y in low nibble.
     * wh-byte: (w-1) in high nibble, (h-1) in low nibble.
     */
    @Test
    void testHextileSubrect_readRoundTrip_plain() throws IOException {
        HextileSubrect original = HextileSubrect.newBuilder()
                .pixel(null).x(3).y(5).width(4).height(2).build();
        byte[] bytes = serialize(out -> original.write(out, false));
        HextileSubrect copy = HextileSubrect.read(streamOf(bytes), false, 1);
        assertEquals(original.x(), copy.x());
        assertEquals(original.y(), copy.y());
        assertEquals(original.width(), copy.width());
        assertEquals(original.height(), copy.height());
    }

    @Test
    void testHextileSubrect_readRoundTrip_coloured() throws IOException {
        HextileSubrect original = HextileSubrect.newBuilder()
                .pixel(new byte[]{(byte) 0xCC}).x(0).y(1).width(8).height(3).build();
        byte[] bytes = serialize(out -> original.write(out, true));
        HextileSubrect copy = HextileSubrect.read(streamOf(bytes), true, 1);
        assertArrayEquals(new byte[]{(byte) 0xCC}, copy.pixel());
        assertEquals(original.x(), copy.x());
        assertEquals(original.y(), copy.y());
    }

    @Test
    void testHextileSubrect_equals() {
        HextileSubrect a = HextileSubrect.newBuilder().pixel(new byte[]{1}).x(0).y(0).width(2).height(2).build();
        HextileSubrect b = HextileSubrect.newBuilder().pixel(new byte[]{1}).x(0).y(0).width(2).height(2).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testHextileSubrect_notEquals() {
        HextileSubrect a = HextileSubrect.newBuilder().pixel(null).x(0).y(0).width(2).height(2).build();
        HextileSubrect b = HextileSubrect.newBuilder().pixel(null).x(1).y(0).width(2).height(2).build();
        assertNotEquals(a, b);
    }

    @Test
    void testHextileSubrect_toString() {
        HextileSubrect s = HextileSubrect.newBuilder().pixel(null).x(2).y(3).width(4).height(5).build();
        assertNotNull(s.toString());
    }

    @Test
    void testHextileSubrect_fromBuilder() {
        HextileSubrect orig = HextileSubrect.newBuilder().pixel(new byte[]{0x77}).x(1).y(2).width(3).height(4).build();
        HextileSubrect copy = HextileSubrect.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    // -----------------------------------------------------------------------
    // HextileTile
    // -----------------------------------------------------------------------

    /**
     * HextileTile read round-trip: background-specified only (subencoding=2).
     */
    @Test
    void testHextileTile_readRoundTrip_backgroundOnly() throws IOException {
        HextileTile original = HextileTile.newBuilder()
                .subencoding(HextileTile.SUBENC_BACKGROUND_SPECIFIED)
                .background(new byte[]{0x55}).build();
        byte[] bytes = serialize(original::write);
        HextileTile copy = HextileTile.read(streamOf(bytes), 8, 8, 1);
        assertEquals(HextileTile.SUBENC_BACKGROUND_SPECIFIED, copy.subencoding());
        assertArrayEquals(new byte[]{0x55}, copy.background());
    }

    @Test
    void testHextileTile_readRoundTrip_withSubrects() throws IOException {
        HextileSubrect sr = HextileSubrect.newBuilder().pixel(null).x(2).y(3).width(4).height(2).build();
        HextileTile original = HextileTile.newBuilder()
                .subencoding(HextileTile.SUBENC_BACKGROUND_SPECIFIED | HextileTile.SUBENC_ANY_SUBRECTS)
                .background(new byte[]{0x11})
                .subrects(List.of(sr)).build();
        byte[] bytes = serialize(original::write);
        HextileTile copy = HextileTile.read(streamOf(bytes), 8, 8, 1);
        assertEquals(1, copy.subrects().size());
        assertEquals(sr.x(), copy.subrects().get(0).x());
    }

    @Test
    void testHextileTile_equals() {
        HextileTile a = HextileTile.newBuilder()
                .subencoding(HextileTile.SUBENC_BACKGROUND_SPECIFIED)
                .background(new byte[]{0x33}).build();
        HextileTile b = HextileTile.newBuilder()
                .subencoding(HextileTile.SUBENC_BACKGROUND_SPECIFIED)
                .background(new byte[]{0x33}).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testHextileTile_notEquals() {
        HextileTile a = HextileTile.newBuilder()
                .subencoding(HextileTile.SUBENC_BACKGROUND_SPECIFIED)
                .background(new byte[]{0x11}).build();
        HextileTile b = HextileTile.newBuilder()
                .subencoding(HextileTile.SUBENC_BACKGROUND_SPECIFIED)
                .background(new byte[]{0x22}).build();
        assertNotEquals(a, b);
    }

    @Test
    void testHextileTile_toString() {
        HextileTile t = HextileTile.newBuilder()
                .subencoding(HextileTile.SUBENC_BACKGROUND_SPECIFIED)
                .background(new byte[]{(byte)0xAA}).build();
        assertNotNull(t.toString());
    }

    @Test
    void testHextileTile_fromBuilder() {
        HextileTile orig = HextileTile.newBuilder()
                .subencoding(HextileTile.SUBENC_FOREGROUND_SPECIFIED)
                .foreground(new byte[]{(byte)0xBB}).build();
        HextileTile copy = HextileTile.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    // -----------------------------------------------------------------------
    // SecurityTypes read round-trip
    // -----------------------------------------------------------------------

    /**
     * From rfbproto.rst.txt – SecurityTypes message:
     * <pre>
     * numberOfSecurityTypes : U8
     * securityTypes         : U8 array[numberOfSecurityTypes]
     * </pre>
     * This is part of the server handshake.
     */
    @Test
    void testSecurityTypes_readRoundTrip() throws IOException {
        SecurityTypes original = SecurityTypes.newBuilder()
                .securityTypes(List.of(1, 2)).build();
        byte[] bytes = serialize(original::write);
        SecurityTypes copy = SecurityTypes.read(streamOf(bytes));
        assertEquals(List.of(1, 2), copy.securityTypes());
    }

    @Test
    void testSecurityTypes_equals() {
        SecurityTypes a = SecurityTypes.newBuilder().securityTypes(List.of(2, 16)).build();
        SecurityTypes b = SecurityTypes.newBuilder().securityTypes(List.of(2, 16)).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testSecurityTypes_toString() {
        SecurityTypes s = SecurityTypes.newBuilder().securityTypes(List.of(2)).build();
        assertNotNull(s.toString());
    }

    // -----------------------------------------------------------------------
    // RfbRectangle equality round-trips
    // -----------------------------------------------------------------------

    private static final PixelFormat PIXEL_FORMAT = PixelFormat.newBuilder()
            .bitsPerPixel(32).depth(24).bigEndian(false).trueColour(true)
            .redMax(255).greenMax(255).blueMax(255)
            .redShift(16).greenShift(8).blueShift(0).build();

    @Test
    void testRfbRectangleCopyRect_equals() {
        RfbRectangleCopyRect a = RfbRectangleCopyRect.newBuilder()
                .x(0).y(0).width(10).height(10).srcX(5).srcY(5).build();
        RfbRectangleCopyRect b = RfbRectangleCopyRect.newBuilder()
                .x(0).y(0).width(10).height(10).srcX(5).srcY(5).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
    }

    @Test
    void testRfbRectangleCopyRect_fromBuilder() {
        RfbRectangleCopyRect orig = RfbRectangleCopyRect.newBuilder()
                .x(1).y(2).width(3).height(4).srcX(5).srcY(6).build();
        RfbRectangleCopyRect copy = RfbRectangleCopyRect.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testRfbRectangleDesktopSize_equals() {
        RfbRectangleDesktopSize a = RfbRectangleDesktopSize.newBuilder()
                .x(0).y(0).width(1920).height(1080).build();
        RfbRectangleDesktopSize b = RfbRectangleDesktopSize.newBuilder()
                .x(0).y(0).width(1920).height(1080).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
    }

    @Test
    void testRfbRectangleDesktopSize_fromBuilder() {
        RfbRectangleDesktopSize orig = RfbRectangleDesktopSize.newBuilder()
                .x(0).y(0).width(800).height(600).build();
        RfbRectangleDesktopSize copy = RfbRectangleDesktopSize.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testRfbRectangleLastRect_equals() {
        RfbRectangleLastRect a = RfbRectangleLastRect.newBuilder()
                .x(0).y(0).width(0).height(0).build();
        RfbRectangleLastRect b = RfbRectangleLastRect.newBuilder()
                .x(0).y(0).width(0).height(0).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
    }

    @Test
    void testRfbRectangleZlib_equals() {
        RfbRectangleZlib a = RfbRectangleZlib.newBuilder()
                .x(0).y(0).width(4).height(4).zlibData(new byte[]{1, 2}).build();
        RfbRectangleZlib b = RfbRectangleZlib.newBuilder()
                .x(0).y(0).width(4).height(4).zlibData(new byte[]{1, 2}).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
    }

    @Test
    void testRfbRectangleZrle_equals() {
        RfbRectangleZrle a = RfbRectangleZrle.newBuilder()
                .x(0).y(0).width(4).height(4).zlibData(new byte[]{3, 4}).build();
        RfbRectangleZrle b = RfbRectangleZrle.newBuilder()
                .x(0).y(0).width(4).height(4).zlibData(new byte[]{3, 4}).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
    }

    @Test
    void testRfbRectangleTightFill_equals() {
        RfbRectangleTightFill a = RfbRectangleTightFill.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0)
                .fillColor(new byte[]{(byte)0xFF, 0x00, 0x00}).build();
        RfbRectangleTightFill b = RfbRectangleTightFill.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0)
                .fillColor(new byte[]{(byte)0xFF, 0x00, 0x00}).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
    }

    @Test
    void testRfbRectangleTightFill_fromBuilder() {
        RfbRectangleTightFill orig = RfbRectangleTightFill.newBuilder()
                .x(1).y(2).width(3).height(4).streamResets(1)
                .fillColor(new byte[]{0x10, 0x20, 0x30}).build();
        RfbRectangleTightFill copy = RfbRectangleTightFill.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testRfbRectangleTightJpeg_equals() {
        RfbRectangleTightJpeg a = RfbRectangleTightJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0)
                .jpegData(new byte[]{(byte)0xFF, (byte)0xD8}).build();
        RfbRectangleTightJpeg b = RfbRectangleTightJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0)
                .jpegData(new byte[]{(byte)0xFF, (byte)0xD8}).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
    }

    @Test
    void testRfbRectangleTightPngFill_equals() {
        RfbRectangleTightPngFill a = RfbRectangleTightPngFill.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0)
                .fillColor(new byte[]{0x10, 0x20, 0x30}).build();
        RfbRectangleTightPngFill b = RfbRectangleTightPngFill.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0)
                .fillColor(new byte[]{0x10, 0x20, 0x30}).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
    }

    @Test
    void testRfbRectangleTightPngFill_fromBuilder() {
        RfbRectangleTightPngFill orig = RfbRectangleTightPngFill.newBuilder()
                .x(0).y(0).width(8).height(8).streamResets(2)
                .fillColor(new byte[]{0x40, 0x50, 0x60}).build();
        RfbRectangleTightPngFill copy = RfbRectangleTightPngFill.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testRfbRectangleTightPngJpeg_equals() {
        RfbRectangleTightPngJpeg a = RfbRectangleTightPngJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0)
                .jpegData(new byte[]{0x01, 0x02}).build();
        RfbRectangleTightPngJpeg b = RfbRectangleTightPngJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0)
                .jpegData(new byte[]{0x01, 0x02}).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
    }

    @Test
    void testRfbRectangleTightPngPng_equals() {
        RfbRectangleTightPngPng a = RfbRectangleTightPngPng.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0)
                .pngData(new byte[]{(byte)0x89, 0x50}).build();
        RfbRectangleTightPngPng b = RfbRectangleTightPngPng.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0)
                .pngData(new byte[]{(byte)0x89, 0x50}).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
    }

    @Test
    void testRfbRectangleRaw_equals() {
        RfbRectangleRaw a = RfbRectangleRaw.newBuilder()
                .x(0).y(0).width(1).height(1).pixels(new byte[]{0x42}).build();
        RfbRectangleRaw b = RfbRectangleRaw.newBuilder()
                .x(0).y(0).width(1).height(1).pixels(new byte[]{0x42}).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
    }

    @Test
    void testRfbRectangleRaw_fromBuilder() {
        RfbRectangleRaw orig = RfbRectangleRaw.newBuilder()
                .x(0).y(0).width(2).height(2).pixels(new byte[]{1, 2, 3, 4}).build();
        RfbRectangleRaw copy = RfbRectangleRaw.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testRfbRectangleJpeg_equals() {
        RfbRectangleJpeg a = RfbRectangleJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).data(new byte[]{(byte)0xFF, (byte)0xD8}).build();
        RfbRectangleJpeg b = RfbRectangleJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).data(new byte[]{(byte)0xFF, (byte)0xD8}).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
    }

    @Test
    void testRfbRectangleExtendedDesktopSize_equals() {
        Screen screen = Screen.newBuilder().id(1).x(0).y(0).width(1920).height(1080).flags(0).build();
        RfbRectangleExtendedDesktopSize a = RfbRectangleExtendedDesktopSize.newBuilder()
                .x(0).y(0).width(1920).height(1080).screens(List.of(screen)).build();
        RfbRectangleExtendedDesktopSize b = RfbRectangleExtendedDesktopSize.newBuilder()
                .x(0).y(0).width(1920).height(1080).screens(List.of(screen)).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
    }

    // -----------------------------------------------------------------------
    // Screen equals/toString
    // -----------------------------------------------------------------------

    @Test
    void testScreen_equals() {
        Screen a = Screen.newBuilder().id(1).x(0).y(0).width(1920).height(1080).flags(0).build();
        Screen b = Screen.newBuilder().id(1).x(0).y(0).width(1920).height(1080).flags(0).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
    }

    @Test
    void testScreen_fromBuilder() {
        Screen orig = Screen.newBuilder().id(2).x(10).y(20).width(640).height(480).flags(1).build();
        Screen copy = Screen.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    // -----------------------------------------------------------------------
    // PixelFormat equals/toString
    // -----------------------------------------------------------------------

    @Test
    void testPixelFormat_equals() {
        PixelFormat a = PIXEL_FORMAT;
        PixelFormat b = PixelFormat.newBuilder()
                .bitsPerPixel(32).depth(24).bigEndian(false).trueColour(true)
                .redMax(255).greenMax(255).blueMax(255)
                .redShift(16).greenShift(8).blueShift(0).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
    }

    @Test
    void testPixelFormat_fromBuilder() {
        PixelFormat orig = PIXEL_FORMAT;
        PixelFormat copy = PixelFormat.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    // -----------------------------------------------------------------------
    // ColourMapEntry
    // -----------------------------------------------------------------------

    @Test
    void testColourMapEntry_equals() {
        ColourMapEntry a = ColourMapEntry.newBuilder().red(0xFFFF).green(0).blue(0).build();
        ColourMapEntry b = ColourMapEntry.newBuilder().red(0xFFFF).green(0).blue(0).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
    }

    @Test
    void testColourMapEntry_fromBuilder() {
        ColourMapEntry orig = ColourMapEntry.newBuilder().red(0x1000).green(0x2000).blue(0x3000).build();
        ColourMapEntry copy = ColourMapEntry.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    // -----------------------------------------------------------------------
    // RfbRectangleRre and RfbRectangleCoRre equals
    // -----------------------------------------------------------------------

    @Test
    void testRfbRectangleRre_equals() {
        RreSubrect sr = RreSubrect.newBuilder().pixel(new byte[]{0x55}).x(0).y(0).width(2).height(2).build();
        RfbRectangleRre a = RfbRectangleRre.newBuilder()
                .x(0).y(0).width(4).height(4)
                .background(new byte[]{0x00}).subrects(List.of(sr)).build();
        RfbRectangleRre b = RfbRectangleRre.newBuilder()
                .x(0).y(0).width(4).height(4)
                .background(new byte[]{0x00}).subrects(List.of(sr)).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
    }

}
