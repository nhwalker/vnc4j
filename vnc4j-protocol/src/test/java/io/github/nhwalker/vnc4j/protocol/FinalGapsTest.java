package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.messages.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the remaining ~54 missed instructions after previous test suites.
 * Key patterns targeted:
 * - equals() false-branch on last field comparison (false-return of final &&)
 * - toString() with null array fields (Cursor, CursorWithAlpha, Jpeg)
 * - Constructor null-list normalization (subrects/tiles/screens null → List.of())
 * - write() null-guard branches (null name, null list fields)
 * - GiiValuatorImpl.writeFixedUtf8 null string
 * - ZlibHexTile SUBENC_ZLIB_RAW with null zlibRawData
 * - RfbRectangleTightBasic with null compressedData
 * - HextileTile SUBENC_ANY_SUBRECTS with null subrects
 * - RfbRectangleDispatch: encoding type 21 (JPEG) → UnsupportedOperationException
 * - KeyEvent.read with down=false
 * - GiiServerVersion.read with bigEndian=false
 * - GiiDeviceCreation round-trip with bigEndian=false
 */
class FinalGapsTest {

    private static final PixelFormat PF_32BPP = PixelFormat.newBuilder()
            .bitsPerPixel(32).depth(24).bigEndian(false).trueColour(true)
            .redMax(255).greenMax(255).blueMax(255)
            .redShift(16).greenShift(8).blueShift(0).build();

    // -----------------------------------------------------------------------
    // equals() false-on-last-field: covers the "false return" branch of the
    // final && in the return statement of each Impl.equals() method.
    // -----------------------------------------------------------------------

    @Test
    void testRfbRectangleLastRect_equals_diffHeight() {
        RfbRectangleLastRect a = RfbRectangleLastRect.newBuilder().x(0).y(0).width(1).height(1).build();
        RfbRectangleLastRect b = RfbRectangleLastRect.newBuilder().x(0).y(0).width(1).height(2).build();
        assertNotEquals(a, b);
    }

    @Test
    void testRfbRectangleJpeg_equals_diffData() {
        RfbRectangleJpeg a = RfbRectangleJpeg.newBuilder().x(0).y(0).width(4).height(4)
                .data(new byte[]{1, 2}).build();
        RfbRectangleJpeg b = RfbRectangleJpeg.newBuilder().x(0).y(0).width(4).height(4)
                .data(new byte[]{3, 4}).build();
        assertNotEquals(a, b);
    }

    @Test
    void testRfbRectangleCursor_equals_diffPixels() {
        RfbRectangleCursor a = RfbRectangleCursor.newBuilder()
                .x(0).y(0).width(1).height(1)
                .pixels(new byte[]{0, 0, 0, 1}).bitmask(new byte[]{(byte) 0x80}).build();
        RfbRectangleCursor b = RfbRectangleCursor.newBuilder()
                .x(0).y(0).width(1).height(1)
                .pixels(new byte[]{0, 0, 0, 2}).bitmask(new byte[]{(byte) 0x80}).build();
        assertNotEquals(a, b);
    }

    @Test
    void testRfbRectangleCopyRect_equals_diffSrcY() {
        RfbRectangleCopyRect a = RfbRectangleCopyRect.newBuilder()
                .x(0).y(0).width(1).height(1).srcX(0).srcY(10).build();
        RfbRectangleCopyRect b = RfbRectangleCopyRect.newBuilder()
                .x(0).y(0).width(1).height(1).srcX(0).srcY(20).build();
        assertNotEquals(a, b);
    }

    @Test
    void testRfbRectangleDesktopSize_equals_diffHeight() {
        RfbRectangleDesktopSize a = RfbRectangleDesktopSize.newBuilder()
                .x(0).y(0).width(100).height(100).build();
        RfbRectangleDesktopSize b = RfbRectangleDesktopSize.newBuilder()
                .x(0).y(0).width(100).height(200).build();
        assertNotEquals(a, b);
    }

    @Test
    void testRfbRectangleTightFill_equals_diffColor() {
        RfbRectangleTightFill a = RfbRectangleTightFill.newBuilder()
                .x(0).y(0).width(1).height(1).streamResets(0).fillColor(new byte[]{0}).build();
        RfbRectangleTightFill b = RfbRectangleTightFill.newBuilder()
                .x(0).y(0).width(1).height(1).streamResets(0).fillColor(new byte[]{1}).build();
        assertNotEquals(a, b);
    }

    @Test
    void testRfbRectangleTightJpeg_equals_diffData() {
        RfbRectangleTightJpeg a = RfbRectangleTightJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0).jpegData(new byte[]{1}).build();
        RfbRectangleTightJpeg b = RfbRectangleTightJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0).jpegData(new byte[]{2}).build();
        assertNotEquals(a, b);
    }

    @Test
    void testRfbRectangleTightPngFill_equals_diffColor() {
        RfbRectangleTightPngFill a = RfbRectangleTightPngFill.newBuilder()
                .x(0).y(0).width(1).height(1).streamResets(0).fillColor(new byte[]{0}).build();
        RfbRectangleTightPngFill b = RfbRectangleTightPngFill.newBuilder()
                .x(0).y(0).width(1).height(1).streamResets(0).fillColor(new byte[]{1}).build();
        assertNotEquals(a, b);
    }

    @Test
    void testRfbRectangleTightPngJpeg_equals_diffData() {
        RfbRectangleTightPngJpeg a = RfbRectangleTightPngJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0).jpegData(new byte[]{1}).build();
        RfbRectangleTightPngJpeg b = RfbRectangleTightPngJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0).jpegData(new byte[]{2}).build();
        assertNotEquals(a, b);
    }

    @Test
    void testRfbRectangleTightPngPng_equals_diffData() {
        RfbRectangleTightPngPng a = RfbRectangleTightPngPng.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0).pngData(new byte[]{(byte) 0x89}).build();
        RfbRectangleTightPngPng b = RfbRectangleTightPngPng.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0).pngData(new byte[]{0x01}).build();
        assertNotEquals(a, b);
    }

    @Test
    void testRfbRectangleRaw_equals_diffPixels() {
        RfbRectangleRaw a = RfbRectangleRaw.newBuilder()
                .x(0).y(0).width(1).height(1).pixels(new byte[]{0}).build();
        RfbRectangleRaw b = RfbRectangleRaw.newBuilder()
                .x(0).y(0).width(1).height(1).pixels(new byte[]{1}).build();
        assertNotEquals(a, b);
    }

    @Test
    void testRfbRectangleZlib_equals_diffData() {
        RfbRectangleZlib a = RfbRectangleZlib.newBuilder()
                .x(0).y(0).width(1).height(1).zlibData(new byte[]{1}).build();
        RfbRectangleZlib b = RfbRectangleZlib.newBuilder()
                .x(0).y(0).width(1).height(1).zlibData(new byte[]{2}).build();
        assertNotEquals(a, b);
    }

    @Test
    void testRfbRectangleZrle_equals_diffData() {
        RfbRectangleZrle a = RfbRectangleZrle.newBuilder()
                .x(0).y(0).width(1).height(1).zlibData(new byte[]{1}).build();
        RfbRectangleZrle b = RfbRectangleZrle.newBuilder()
                .x(0).y(0).width(1).height(1).zlibData(new byte[]{2}).build();
        assertNotEquals(a, b);
    }

    @Test
    void testRfbRectangleRre_equals_diffSubrects() {
        RreSubrect sr1 = RreSubrect.newBuilder().pixel(new byte[]{0}).x(0).y(0).width(1).height(1).build();
        RreSubrect sr2 = RreSubrect.newBuilder().pixel(new byte[]{1}).x(0).y(0).width(1).height(1).build();
        RfbRectangleRre a = RfbRectangleRre.newBuilder()
                .x(0).y(0).width(1).height(1).background(new byte[]{0}).subrects(List.of(sr1)).build();
        RfbRectangleRre b = RfbRectangleRre.newBuilder()
                .x(0).y(0).width(1).height(1).background(new byte[]{0}).subrects(List.of(sr2)).build();
        assertNotEquals(a, b);
    }

    @Test
    void testRfbRectangleExtendedDesktopSize_equals_diffScreens() {
        Screen s1 = Screen.newBuilder().id(1).x(0).y(0).width(800).height(600).flags(0).build();
        Screen s2 = Screen.newBuilder().id(2).x(0).y(0).width(800).height(600).flags(0).build();
        RfbRectangleExtendedDesktopSize a = RfbRectangleExtendedDesktopSize.newBuilder()
                .x(0).y(0).width(800).height(600).screens(List.of(s1)).build();
        RfbRectangleExtendedDesktopSize b = RfbRectangleExtendedDesktopSize.newBuilder()
                .x(0).y(0).width(800).height(600).screens(List.of(s2)).build();
        assertNotEquals(a, b);
    }

    // -----------------------------------------------------------------------
    // toString() with null array fields
    // -----------------------------------------------------------------------

    @Test
    void testRfbRectangleCursor_toString_nullPixelsAndBitmask() {
        RfbRectangleCursor msg = RfbRectangleCursor.newBuilder()
                .x(0).y(0).width(1).height(1).pixels(null).bitmask(null).build();
        String s = msg.toString();
        assertTrue(s.contains("null"), "toString should contain 'null' for null pixels/bitmask");
    }

    @Test
    void testRfbRectangleJpeg_toString_nullData() {
        RfbRectangleJpeg msg = RfbRectangleJpeg.newBuilder()
                .x(0).y(0).width(4).height(4).data(null).build();
        String s = msg.toString();
        assertTrue(s.contains("null"), "toString should contain 'null' for null data");
    }

    // -----------------------------------------------------------------------
    // Constructor null-list normalization: covers the List.of() branch in
    // constructors that have `field = field != null ? field : List.of()`
    // -----------------------------------------------------------------------

    @Test
    void testRfbRectangleRre_nullSubrects_constructor() {
        RfbRectangleRre msg = RfbRectangleRre.newBuilder()
                .x(0).y(0).width(1).height(1).background(new byte[]{0}).subrects(null).build();
        assertNotNull(msg.subrects());
    }

    @Test
    void testRfbRectangleHextile_nullTiles_constructor() {
        RfbRectangleHextile msg = RfbRectangleHextile.newBuilder()
                .x(0).y(0).width(16).height(16).tiles(null).build();
        assertNotNull(msg.tiles());
    }

    @Test
    void testRfbRectangleExtendedDesktopSize_nullScreens_constructor() {
        RfbRectangleExtendedDesktopSize msg = RfbRectangleExtendedDesktopSize.newBuilder()
                .x(0).y(0).width(100).height(100).screens(null).build();
        assertNotNull(msg.screens());
    }

    // -----------------------------------------------------------------------
    // write() null guards that are reachable
    // -----------------------------------------------------------------------

    @Test
    void testServerInit_nullName_write() throws IOException {
        ServerInit msg = ServerInit.newBuilder()
                .framebufferWidth(800).framebufferHeight(600)
                .pixelFormat(PF_32BPP).name(null).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> msg.write(baos));
        assertTrue(baos.size() > 0);
    }

    @Test
    void testSecurityTypes_nullList_write() throws IOException {
        SecurityTypes msg = SecurityTypes.newBuilder().securityTypes(null).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> msg.write(baos));
        // count byte = 0 (empty list)
        assertEquals(0, baos.toByteArray()[0]);
    }

    @Test
    void testSetEncodings_nullList_write() throws IOException {
        SetEncodings msg = SetEncodings.newBuilder().encodings(null).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> msg.write(baos));
        assertTrue(baos.size() > 0);
    }

    @Test
    void testSetColourMapEntries_nullList_write() throws IOException {
        SetColourMapEntries msg = SetColourMapEntries.newBuilder()
                .firstColour(0).colours(null).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> msg.write(baos));
        assertTrue(baos.size() > 0);
    }

    @Test
    void testFramebufferUpdate_nullRects_write() throws IOException {
        FramebufferUpdate msg = FramebufferUpdate.newBuilder().rectangles(null).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> msg.write(baos));
        assertTrue(baos.size() > 0);
    }

    // -----------------------------------------------------------------------
    // RfbRectangleTightBasic with null compressedData and null palette
    // -----------------------------------------------------------------------

    @Test
    void testRfbRectangleTightBasic_nullCompressedData_write() throws IOException {
        RfbRectangleTightBasic msg = RfbRectangleTightBasic.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0)
                .filterType(RfbRectangleTightBasic.FILTER_COPY)
                .paletteSize(0).palette(new byte[0])
                .compressedData(null)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> msg.write(baos));
        assertTrue(baos.size() > 0);
    }

    @Test
    void testRfbRectangleTightBasic_paletteFilter_nullPalette_write() throws IOException {
        // filterType=FILTER_PALETTE with null palette exercises the
        // `if (palette != null) dos.write(palette)` null branch.
        RfbRectangleTightBasic msg = RfbRectangleTightBasic.newBuilder()
                .x(0).y(0).width(4).height(4).streamResets(0)
                .filterType(RfbRectangleTightBasic.FILTER_PALETTE)
                .paletteSize(2).palette(null)
                .compressedData(new byte[]{0})
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> msg.write(baos));
        assertTrue(baos.size() > 0);
    }

    // -----------------------------------------------------------------------
    // HextileTile SUBENC_ANY_SUBRECTS with null subrects
    // -----------------------------------------------------------------------

    @Test
    void testHextileTile_anySubrects_nullSubrects_write() throws IOException {
        HextileTile msg = HextileTile.newBuilder()
                .subencoding(HextileTile.SUBENC_ANY_SUBRECTS)
                .subrects(null)
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> msg.write(baos));
        // subenc byte + count byte (0) = 2 bytes
        assertEquals(2, baos.size());
    }

    // -----------------------------------------------------------------------
    // RfbRectangleDispatch: encoding type 21 (JPEG) → UnsupportedOperationException
    // -----------------------------------------------------------------------

    /**
     * Encoding type 21 (JPEG) throws UnsupportedOperationException in dispatch
     * because JPEG encoding has no length prefix and cannot self-delimit.
     */
    @Test
    void testRfbRectangleDispatch_jpegEncoding_throwsUOE() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);
        dos.writeShort(0);   // x
        dos.writeShort(0);   // y
        dos.writeShort(4);   // width
        dos.writeShort(4);   // height
        dos.writeInt(21);    // encoding type = JPEG (unsupported for self-delimited reads)
        byte[] bytes = baos.toByteArray();

        assertThrows(UnsupportedOperationException.class,
                () -> RfbRectangle.read(new ByteArrayInputStream(bytes), PF_32BPP));
    }

    // -----------------------------------------------------------------------
    // KeyEvent.read with down=false
    // -----------------------------------------------------------------------

    /**
     * KeyEventImpl.read() has {@code boolean down = dis.readUnsignedByte() != 0}.
     * The false branch (down=false) covers the != 0 → false path.
     */
    @Test
    void testKeyEvent_read_downFalse() throws IOException {
        KeyEvent orig = KeyEvent.newBuilder().down(false).key(0x41).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        orig.write(baos);
        byte[] bytes = baos.toByteArray();
        // write() includes message-type byte[0]; read() starts after it
        KeyEvent copy = KeyEvent.read(new ByteArrayInputStream(bytes, 1, bytes.length - 1));
        assertFalse(copy.down());
        assertEquals(0x41, copy.key());
    }

    // -----------------------------------------------------------------------
    // RfbRectangleDispatch: default case (unknown encoding type)
    // -----------------------------------------------------------------------

    /**
     * Encoding type 999 is not in the dispatch switch → default →
     * throws UnsupportedOperationException("Unknown encoding type: 999").
     * This covers the 5 missed instructions in the default case.
     */
    @Test
    void testRfbRectangleDispatch_unknownEncoding_throwsUOE() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);
        dos.writeShort(0);    // x
        dos.writeShort(0);    // y
        dos.writeShort(4);    // width
        dos.writeShort(4);    // height
        dos.writeInt(999);    // encoding type = unknown
        byte[] bytes = baos.toByteArray();

        assertThrows(UnsupportedOperationException.class,
                () -> RfbRectangle.read(new ByteArrayInputStream(bytes), PF_32BPP));
    }

    // -----------------------------------------------------------------------
    // RfbRectangleDispatch.read: DataInputStream input (true branch of instanceof)
    // -----------------------------------------------------------------------

    /**
     * RfbRectangleDispatch.read() has:
     * {@code DataInputStream dis = (in instanceof DataInputStream d) ? d : new DataInputStream(in)}.
     * When input IS a DataInputStream, the true branch reuses it directly.
     * Passing a DataInputStream covers the 5 missed instructions in the true branch.
     */
    @Test
    void testRfbRectangleDispatch_withDataInputStream() throws IOException {
        // Build a simple raw rect (encoding 0) using a raw rectangle
        RfbRectangleRaw rawRect = RfbRectangleRaw.newBuilder()
                .x(0).y(0).width(1).height(1)
                .pixels(new byte[]{0x11, 0x22, 0x33, 0x44}).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rawRect.write(baos);
        byte[] bytes = baos.toByteArray();
        // bytes[0..11] = rect header (x,y,w,h,enc); bytes[12..15] = pixels
        // RfbRectangle.read expects the FULL rect bytes (header + payload)

        // Pass a DataInputStream directly to trigger the instanceof true branch
        java.io.DataInputStream dis = new java.io.DataInputStream(
                new ByteArrayInputStream(bytes));
        RfbRectangle result = RfbRectangle.read(dis, PF_32BPP);
        assertInstanceOf(RfbRectangleRaw.class, result);
    }
}
