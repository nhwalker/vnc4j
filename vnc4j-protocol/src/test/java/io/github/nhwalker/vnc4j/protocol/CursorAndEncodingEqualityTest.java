package io.github.nhwalker.vnc4j.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Equality, hashCode, toString, from-builder, and read round-trip tests for:
 * RfbRectangleCursor, RfbRectangleHextile, and RfbRectangleTightBasic.
 *
 * <p>From rfbproto.rst.txt – Cursor pseudo-encoding (type -239):
 * <pre>
 * x, y       : hotspot coordinates
 * width, height : cursor dimensions
 * pixels     : width × height × bytesPerPixel bytes
 * bitmask    : ⌈width/8⌉ × height bytes
 * </pre>
 */
class CursorAndEncodingEqualityTest {

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
    // RfbRectangleCursor
    // -----------------------------------------------------------------------

    @Test
    void testCursor_equals() {
        RfbRectangleCursor a = RfbRectangleCursor.newBuilder()
                .x(3).y(5).width(4).height(4)
                .pixels(new byte[]{1, 2, 3, 4})
                .bitmask(new byte[]{(byte)0xF0}).build();
        RfbRectangleCursor b = RfbRectangleCursor.newBuilder()
                .x(3).y(5).width(4).height(4)
                .pixels(new byte[]{1, 2, 3, 4})
                .bitmask(new byte[]{(byte)0xF0}).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testCursor_notEquals_differentPixels() {
        RfbRectangleCursor a = RfbRectangleCursor.newBuilder()
                .x(0).y(0).width(2).height(2)
                .pixels(new byte[]{1, 2}).bitmask(new byte[]{0}).build();
        RfbRectangleCursor b = RfbRectangleCursor.newBuilder()
                .x(0).y(0).width(2).height(2)
                .pixels(new byte[]{3, 4}).bitmask(new byte[]{0}).build();
        assertNotEquals(a, b);
    }

    @Test
    void testCursor_toString() {
        RfbRectangleCursor c = RfbRectangleCursor.newBuilder()
                .x(1).y(2).width(4).height(4)
                .pixels(new byte[4]).bitmask(new byte[2]).build();
        assertNotNull(c.toString());
    }

    @Test
    void testCursor_fromBuilder() {
        RfbRectangleCursor orig = RfbRectangleCursor.newBuilder()
                .x(5).y(7).width(8).height(8)
                .pixels(new byte[8]).bitmask(new byte[8]).build();
        RfbRectangleCursor copy = RfbRectangleCursor.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    /**
     * Read round-trip for Cursor with 8bpp (1 byte per pixel).
     * Cursor pixels = width × height × bpp = 2 × 2 × 1 = 4 bytes
     * Bitmask = ⌈width/8⌉ × height = 1 × 2 = 2 bytes
     */
    @Test
    void testCursor_readRoundTrip_8bpp() throws IOException {
        RfbRectangleCursor orig = RfbRectangleCursor.newBuilder()
                .x(0).y(0).width(2).height(2)
                .pixels(new byte[]{0x11, 0x22, 0x33, 0x44})
                .bitmask(new byte[]{(byte)0xC0, (byte)0xC0}).build();
        byte[] bytes = serialize(orig::write);
        // Skip the 12-byte header (RfbRectangle.read expects a full rectangle stream)
        RfbRectangle result = RfbRectangle.read(streamOf(bytes), PF_8BPP);
        assertInstanceOf(RfbRectangleCursor.class, result);
        RfbRectangleCursor copy = (RfbRectangleCursor) result;
        assertEquals(orig, copy);
    }

    // -----------------------------------------------------------------------
    // RfbRectangleHextile
    // -----------------------------------------------------------------------

    @Test
    void testHextile_equals() {
        HextileTile tile = HextileTile.newBuilder()
                .subencoding(HextileTile.SUBENC_BACKGROUND_SPECIFIED)
                .background(new byte[]{0x55}).build();
        RfbRectangleHextile a = RfbRectangleHextile.newBuilder()
                .x(0).y(0).width(4).height(4).tiles(List.of(tile)).build();
        RfbRectangleHextile b = RfbRectangleHextile.newBuilder()
                .x(0).y(0).width(4).height(4).tiles(List.of(tile)).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testHextile_notEquals() {
        HextileTile tile1 = HextileTile.newBuilder()
                .subencoding(HextileTile.SUBENC_BACKGROUND_SPECIFIED)
                .background(new byte[]{0x11}).build();
        HextileTile tile2 = HextileTile.newBuilder()
                .subencoding(HextileTile.SUBENC_BACKGROUND_SPECIFIED)
                .background(new byte[]{0x22}).build();
        RfbRectangleHextile a = RfbRectangleHextile.newBuilder()
                .x(0).y(0).width(4).height(4).tiles(List.of(tile1)).build();
        RfbRectangleHextile b = RfbRectangleHextile.newBuilder()
                .x(0).y(0).width(4).height(4).tiles(List.of(tile2)).build();
        assertNotEquals(a, b);
    }

    @Test
    void testHextile_toString() {
        RfbRectangleHextile h = RfbRectangleHextile.newBuilder()
                .x(0).y(0).width(4).height(4).tiles(List.of()).build();
        assertNotNull(h.toString());
    }

    @Test
    void testHextile_fromBuilder() {
        HextileTile tile = HextileTile.newBuilder()
                .subencoding(HextileTile.SUBENC_FOREGROUND_SPECIFIED)
                .foreground(new byte[]{0x33}).build();
        RfbRectangleHextile orig = RfbRectangleHextile.newBuilder()
                .x(1).y(2).width(3).height(4).tiles(List.of(tile)).build();
        RfbRectangleHextile copy = RfbRectangleHextile.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    /**
     * Read round-trip for a Hextile rectangle (4×4, 1bpp, single tile with BackgroundSpecified).
     * Wire: 12-byte header + 1 tile (subenc=2, 1 bg byte).
     */
    @Test
    void testHextile_readRoundTrip() throws IOException {
        HextileTile tile = HextileTile.newBuilder()
                .subencoding(HextileTile.SUBENC_BACKGROUND_SPECIFIED)
                .background(new byte[]{0x7F}).build();
        RfbRectangleHextile orig = RfbRectangleHextile.newBuilder()
                .x(0).y(0).width(4).height(4).tiles(List.of(tile)).build();
        byte[] bytes = serialize(orig::write);
        RfbRectangle result = RfbRectangle.read(streamOf(bytes), PF_8BPP);
        assertInstanceOf(RfbRectangleHextile.class, result);
        RfbRectangleHextile copy = (RfbRectangleHextile) result;
        assertEquals(orig, copy);
    }

    // -----------------------------------------------------------------------
    // RfbRectangleTightBasic
    // -----------------------------------------------------------------------

    /**
     * TightBasic with CopyFilter (filterType=0): ctrl=0x00 (no ReadFilter bit set),
     * compact-length prefix, then zlib-compressed data.
     * <p>
     * Per rfbproto.rst.txt, ReadFilter bit (0x40) is set in ctrl only for PALETTE and GRADIENT.
     * For COPY filter, no ReadFilter bit, no filter byte in the stream.
     */
    @Test
    void testTightBasic_equals_copyFilter() {
        RfbRectangleTightBasic a = RfbRectangleTightBasic.newBuilder()
                .x(0).y(0).width(4).height(4)
                .streamResets(0).streamNumber(0)
                .filterType(RfbRectangleTightBasic.FILTER_COPY).paletteSize(0)
                .palette(null).compressedData(new byte[]{1, 2, 3}).build();
        RfbRectangleTightBasic b = RfbRectangleTightBasic.newBuilder()
                .x(0).y(0).width(4).height(4)
                .streamResets(0).streamNumber(0)
                .filterType(RfbRectangleTightBasic.FILTER_COPY).paletteSize(0)
                .palette(null).compressedData(new byte[]{1, 2, 3}).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testTightBasic_notEquals() {
        RfbRectangleTightBasic a = RfbRectangleTightBasic.newBuilder()
                .x(0).y(0).width(4).height(4)
                .streamResets(0).streamNumber(0)
                .filterType(RfbRectangleTightBasic.FILTER_COPY).paletteSize(0)
                .palette(null).compressedData(new byte[]{1}).build();
        RfbRectangleTightBasic b = RfbRectangleTightBasic.newBuilder()
                .x(0).y(0).width(4).height(4)
                .streamResets(0).streamNumber(0)
                .filterType(RfbRectangleTightBasic.FILTER_COPY).paletteSize(0)
                .palette(null).compressedData(new byte[]{2}).build();
        assertNotEquals(a, b);
    }

    @Test
    void testTightBasic_toString() {
        RfbRectangleTightBasic t = RfbRectangleTightBasic.newBuilder()
                .x(0).y(0).width(4).height(4)
                .streamResets(0).streamNumber(1)
                .filterType(RfbRectangleTightBasic.FILTER_GRADIENT).paletteSize(0)
                .palette(null).compressedData(new byte[]{5, 6}).build();
        assertNotNull(t.toString());
    }

    @Test
    void testTightBasic_fromBuilder() {
        RfbRectangleTightBasic orig = RfbRectangleTightBasic.newBuilder()
                .x(0).y(0).width(4).height(4)
                .streamResets(1).streamNumber(2)
                .filterType(RfbRectangleTightBasic.FILTER_COPY).paletteSize(0)
                .palette(null).compressedData(new byte[]{7, 8, 9}).build();
        RfbRectangleTightBasic copy = RfbRectangleTightBasic.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    /**
     * Read round-trip for TightBasic with CopyFilter via RfbRectangle.read.
     */
    @Test
    void testTightBasic_readRoundTrip_copyFilter() throws IOException {
        RfbRectangleTightBasic orig = RfbRectangleTightBasic.newBuilder()
                .x(0).y(0).width(4).height(4)
                .streamResets(0).streamNumber(0)
                .filterType(RfbRectangleTightBasic.FILTER_COPY).paletteSize(0)
                .palette(null).compressedData(new byte[]{0x78, (byte)0x9C, 0x01}).build();
        byte[] bytes = serialize(orig::write);
        RfbRectangle result = RfbRectangle.read(streamOf(bytes), PF_32BPP);
        assertInstanceOf(RfbRectangleTightBasic.class, result);
        RfbRectangleTightBasic copy = (RfbRectangleTightBasic) result;
        assertEquals(orig.filterType(), copy.filterType());
        assertEquals(orig.paletteSize(), copy.paletteSize());
        assertArrayEquals(orig.compressedData(), copy.compressedData());
    }

    /**
     * Read round-trip for TightBasic with PaletteFilter.
     * With PaletteFilter: ctrl has ReadFilter (0x40) set, then filter-byte (1),
     * then paletteSize byte (N), then N TPIXEL entries, then compact-length + data.
     */
    @Test
    void testTightBasic_readRoundTrip_paletteFilter() throws IOException {
        // 2 palette entries × 3 bytes (TPIXEL for 32bpp depth-24 true-colour)
        byte[] palette = {(byte)0xFF, 0x00, 0x00, 0x00, (byte)0xFF, 0x00};
        RfbRectangleTightBasic orig = RfbRectangleTightBasic.newBuilder()
                .x(0).y(0).width(4).height(4)
                .streamResets(0).streamNumber(0)
                .filterType(RfbRectangleTightBasic.FILTER_PALETTE).paletteSize(2)
                .palette(palette).compressedData(new byte[]{0x05}).build();
        byte[] bytes = serialize(orig::write);
        RfbRectangle result = RfbRectangle.read(streamOf(bytes), PF_32BPP);
        assertInstanceOf(RfbRectangleTightBasic.class, result);
        RfbRectangleTightBasic copy = (RfbRectangleTightBasic) result;
        assertEquals(RfbRectangleTightBasic.FILTER_PALETTE, copy.filterType());
        assertEquals(2, copy.paletteSize());
        assertArrayEquals(palette, copy.palette());
    }
}
