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
 * RfbRectangleCursor, RfbRectangleXCursor, RfbRectangleCursorWithAlpha,
 * RfbRectangleHextile, RfbRectangleZlibHex, and RfbRectangleTightBasic.
 *
 * <p>From rfbproto.rst.txt – Cursor pseudo-encoding (type -239):
 * <pre>
 * x, y       : hotspot coordinates
 * width, height : cursor dimensions
 * pixels     : width × height × bytesPerPixel bytes
 * bitmask    : ⌈width/8⌉ × height bytes
 * </pre>
 *
 * <p>XCursor pseudo-encoding (type -240) with non-zero width and height:
 * <pre>
 * primaryR, G, B   : U8 each (foreground colour)
 * secondaryR, G, B : U8 each (background colour)
 * bitmap   : ⌈width/8⌉ × height bytes
 * bitmask  : ⌈width/8⌉ × height bytes
 * </pre>
 *
 * <p>CursorWithAlpha (type -314):
 * <pre>
 * S32 encoding : nested encoding type (e.g. 0 = Raw)
 * data         : raw RGBA pixel data (4 bytes per pixel)
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
    // RfbRectangleXCursor
    // -----------------------------------------------------------------------

    /**
     * XCursor with non-zero dimensions: colours (6 bytes) + bitmap (⌈w/8⌉×h) + bitmask (⌈w/8⌉×h).
     */
    @Test
    void testXCursor_equals() {
        RfbRectangleXCursor a = RfbRectangleXCursor.newBuilder()
                .x(0).y(0).width(8).height(4)
                .primaryR(255).primaryG(255).primaryB(255)
                .secondaryR(0).secondaryG(0).secondaryB(0)
                .bitmap(new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF})
                .bitmask(new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF}).build();
        RfbRectangleXCursor b = RfbRectangleXCursor.newBuilder()
                .x(0).y(0).width(8).height(4)
                .primaryR(255).primaryG(255).primaryB(255)
                .secondaryR(0).secondaryG(0).secondaryB(0)
                .bitmap(new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF})
                .bitmask(new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF}).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testXCursor_notEquals_differentPrimary() {
        RfbRectangleXCursor a = RfbRectangleXCursor.newBuilder()
                .x(0).y(0).width(8).height(1)
                .primaryR(255).primaryG(0).primaryB(0)
                .secondaryR(0).secondaryG(0).secondaryB(0)
                .bitmap(new byte[]{(byte)0xFF}).bitmask(new byte[]{(byte)0xFF}).build();
        RfbRectangleXCursor b = RfbRectangleXCursor.newBuilder()
                .x(0).y(0).width(8).height(1)
                .primaryR(0).primaryG(255).primaryB(0)
                .secondaryR(0).secondaryG(0).secondaryB(0)
                .bitmap(new byte[]{(byte)0xFF}).bitmask(new byte[]{(byte)0xFF}).build();
        assertNotEquals(a, b);
    }

    @Test
    void testXCursor_toString() {
        RfbRectangleXCursor c = RfbRectangleXCursor.newBuilder()
                .x(0).y(0).width(8).height(1)
                .primaryR(128).primaryG(64).primaryB(32)
                .secondaryR(0).secondaryG(0).secondaryB(0)
                .bitmap(new byte[]{0x7F}).bitmask(new byte[]{(byte)0xFF}).build();
        assertNotNull(c.toString());
    }

    @Test
    void testXCursor_fromBuilder() {
        RfbRectangleXCursor orig = RfbRectangleXCursor.newBuilder()
                .x(1).y(2).width(8).height(2)
                .primaryR(200).primaryG(100).primaryB(50)
                .secondaryR(10).secondaryG(20).secondaryB(30)
                .bitmap(new byte[]{0x55, 0x55})
                .bitmask(new byte[]{(byte)0xFF, (byte)0xFF}).build();
        RfbRectangleXCursor copy = RfbRectangleXCursor.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testXCursor_readRoundTrip_withData() throws IOException {
        // 8-wide, 1-tall cursor: colours=6 bytes, bitmap=1 byte, bitmask=1 byte
        RfbRectangleXCursor orig = RfbRectangleXCursor.newBuilder()
                .x(0).y(0).width(8).height(1)
                .primaryR(255).primaryG(255).primaryB(255)
                .secondaryR(0).secondaryG(0).secondaryB(0)
                .bitmap(new byte[]{(byte)0xAA})
                .bitmask(new byte[]{(byte)0xFF}).build();
        byte[] bytes = serialize(orig::write);
        RfbRectangle result = RfbRectangle.read(streamOf(bytes), PF_32BPP);
        assertInstanceOf(RfbRectangleXCursor.class, result);
        RfbRectangleXCursor copy = (RfbRectangleXCursor) result;
        assertEquals(255, copy.primaryR());
        assertEquals(orig, copy);
    }

    @Test
    void testXCursor_readRoundTrip_zeroDimensions() throws IOException {
        // Zero-size cursor: no colour or bitmap bytes
        RfbRectangleXCursor orig = RfbRectangleXCursor.newBuilder()
                .x(0).y(0).width(0).height(0)
                .primaryR(0).primaryG(0).primaryB(0)
                .secondaryR(0).secondaryG(0).secondaryB(0)
                .bitmap(null).bitmask(null).build();
        byte[] bytes = serialize(orig::write);
        RfbRectangle result = RfbRectangle.read(streamOf(bytes), PF_32BPP);
        assertInstanceOf(RfbRectangleXCursor.class, result);
        assertEquals(0, result.width());
        assertEquals(0, result.height());
    }

    // -----------------------------------------------------------------------
    // RfbRectangleCursorWithAlpha
    // -----------------------------------------------------------------------

    @Test
    void testCursorWithAlpha_equals() {
        RfbRectangleCursorWithAlpha a = RfbRectangleCursorWithAlpha.newBuilder()
                .x(0).y(0).width(2).height(2)
                .encoding(0).data(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}).build();
        RfbRectangleCursorWithAlpha b = RfbRectangleCursorWithAlpha.newBuilder()
                .x(0).y(0).width(2).height(2)
                .encoding(0).data(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testCursorWithAlpha_notEquals() {
        RfbRectangleCursorWithAlpha a = RfbRectangleCursorWithAlpha.newBuilder()
                .x(0).y(0).width(1).height(1).encoding(0).data(new byte[]{1, 2, 3, 4}).build();
        RfbRectangleCursorWithAlpha b = RfbRectangleCursorWithAlpha.newBuilder()
                .x(0).y(0).width(1).height(1).encoding(0).data(new byte[]{5, 6, 7, 8}).build();
        assertNotEquals(a, b);
    }

    @Test
    void testCursorWithAlpha_toString() {
        RfbRectangleCursorWithAlpha c = RfbRectangleCursorWithAlpha.newBuilder()
                .x(0).y(0).width(1).height(1).encoding(0).data(new byte[4]).build();
        assertNotNull(c.toString());
    }

    @Test
    void testCursorWithAlpha_fromBuilder() {
        RfbRectangleCursorWithAlpha orig = RfbRectangleCursorWithAlpha.newBuilder()
                .x(3).y(5).width(2).height(2).encoding(0).data(new byte[16]).build();
        RfbRectangleCursorWithAlpha copy = RfbRectangleCursorWithAlpha.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testCursorWithAlpha_readRoundTrip() throws IOException {
        // 1×1 raw RGBA cursor = 4 bytes
        byte[] rgba = {(byte)0xFF, 0x00, 0x00, (byte)0xFF};
        RfbRectangleCursorWithAlpha orig = RfbRectangleCursorWithAlpha.newBuilder()
                .x(0).y(0).width(1).height(1).encoding(0).data(rgba).build();
        byte[] bytes = serialize(orig::write);
        RfbRectangle result = RfbRectangle.read(streamOf(bytes), PF_32BPP);
        assertInstanceOf(RfbRectangleCursorWithAlpha.class, result);
        RfbRectangleCursorWithAlpha copy = (RfbRectangleCursorWithAlpha) result;
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
    // RfbRectangleZlibHex
    // -----------------------------------------------------------------------

    @Test
    void testZlibHex_equals() {
        ZlibHexTile tile = ZlibHexTile.newBuilder()
                .subencoding(ZlibHexTile.SUBENC_ZLIB_RAW)
                .zlibRawData(new byte[]{0x78, (byte)0x9C, 0x01}).build();
        RfbRectangleZlibHex a = RfbRectangleZlibHex.newBuilder()
                .x(0).y(0).width(4).height(4).tiles(List.of(tile)).build();
        RfbRectangleZlibHex b = RfbRectangleZlibHex.newBuilder()
                .x(0).y(0).width(4).height(4).tiles(List.of(tile)).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testZlibHex_notEquals() {
        ZlibHexTile t1 = ZlibHexTile.newBuilder()
                .subencoding(ZlibHexTile.SUBENC_ZLIB_RAW)
                .zlibRawData(new byte[]{0x01}).build();
        ZlibHexTile t2 = ZlibHexTile.newBuilder()
                .subencoding(ZlibHexTile.SUBENC_ZLIB_RAW)
                .zlibRawData(new byte[]{0x02}).build();
        RfbRectangleZlibHex a = RfbRectangleZlibHex.newBuilder()
                .x(0).y(0).width(4).height(4).tiles(List.of(t1)).build();
        RfbRectangleZlibHex b = RfbRectangleZlibHex.newBuilder()
                .x(0).y(0).width(4).height(4).tiles(List.of(t2)).build();
        assertNotEquals(a, b);
    }

    @Test
    void testZlibHex_toString() {
        RfbRectangleZlibHex h = RfbRectangleZlibHex.newBuilder()
                .x(0).y(0).width(4).height(4).tiles(List.of()).build();
        assertNotNull(h.toString());
    }

    @Test
    void testZlibHex_fromBuilder() {
        ZlibHexTile tile = ZlibHexTile.newBuilder()
                .subencoding(ZlibHexTile.SUBENC_RAW)
                .rawPixels(new byte[]{1, 2, 3, 4}).build();
        RfbRectangleZlibHex orig = RfbRectangleZlibHex.newBuilder()
                .x(1).y(2).width(3).height(4).tiles(List.of(tile)).build();
        RfbRectangleZlibHex copy = RfbRectangleZlibHex.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    /**
     * Read round-trip for ZlibHex with a RAW tile (4×4, 1bpp, single tile).
     */
    @Test
    void testZlibHex_readRoundTrip_rawTile() throws IOException {
        byte[] pixels = new byte[16]; // 4×4 × 1bpp = 16 bytes
        for (int i = 0; i < pixels.length; i++) pixels[i] = (byte) i;
        ZlibHexTile tile = ZlibHexTile.newBuilder()
                .subencoding(ZlibHexTile.SUBENC_RAW)
                .rawPixels(pixels).build();
        RfbRectangleZlibHex orig = RfbRectangleZlibHex.newBuilder()
                .x(0).y(0).width(4).height(4).tiles(List.of(tile)).build();
        byte[] bytes = serialize(orig::write);
        RfbRectangle result = RfbRectangle.read(streamOf(bytes), PF_8BPP);
        assertInstanceOf(RfbRectangleZlibHex.class, result);
        RfbRectangleZlibHex copy = (RfbRectangleZlibHex) result;
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
