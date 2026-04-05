package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.ClientMessageDispatch;
import io.github.nhwalker.vnc4j.protocol.internal.GiiEventDispatch;
import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleDispatch;
import io.github.nhwalker.vnc4j.protocol.internal.ServerMessageDispatch;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the dispatch classes: {@link ClientMessageDispatch}, {@link ServerMessageDispatch},
 * {@link RfbRectangleDispatch}, and {@link GiiEventDispatch}.
 *
 * <p>Each dispatch class reads the message-type byte and routes to the correct reader.
 * These tests verify that the round-trip through the dispatcher produces the correct
 * concrete type.
 *
 * <p>From rfbproto.rst.txt – client message types: 0=SetPixelFormat, 2=SetEncodings,
 * 3=FramebufferUpdateRequest, 4=KeyEvent, 5=PointerEvent, 6=ClientCutText.
 *
 * <p>From rfbproto.rst.txt – server message types: 0=FramebufferUpdate, 1=SetColourMapEntries,
 * 2=Bell, 3=ServerCutText.
 */
class DispatchTest {

    /** Standard 32-bit true-colour pixel format used throughout these tests. */
    private static final PixelFormat PIXEL_FORMAT = PixelFormat.newBuilder()
            .bitsPerPixel(32).depth(24).bigEndian(false).trueColour(true)
            .redMax(255).greenMax(255).blueMax(255)
            .redShift(16).greenShift(8).blueShift(0)
            .build();

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
    // ClientMessageDispatch
    // -----------------------------------------------------------------------

    @Test
    void testClientDispatch_setPixelFormat() throws IOException {
        PixelFormat pf = PixelFormat.newBuilder()
                .bitsPerPixel(32).depth(24).bigEndian(false).trueColour(true)
                .redMax(255).greenMax(255).blueMax(255)
                .redShift(16).greenShift(8).blueShift(0).build();
        SetPixelFormat msg = SetPixelFormat.newBuilder().pixelFormat(pf).build();
        byte[] bytes = serialize(msg::write);
        ClientMessage result = ClientMessageDispatch.read(streamOf(bytes));
        assertInstanceOf(SetPixelFormat.class, result);
        assertEquals(32, ((SetPixelFormat) result).pixelFormat().bitsPerPixel());
    }

    @Test
    void testClientDispatch_setEncodings() throws IOException {
        SetEncodings msg = SetEncodings.newBuilder().encodings(List.of(0, 1, 2)).build();
        byte[] bytes = serialize(msg::write);
        ClientMessage result = ClientMessageDispatch.read(streamOf(bytes));
        assertInstanceOf(SetEncodings.class, result);
        assertEquals(List.of(0, 1, 2), ((SetEncodings) result).encodings());
    }

    @Test
    void testClientDispatch_framebufferUpdateRequest() throws IOException {
        FramebufferUpdateRequest msg = FramebufferUpdateRequest.newBuilder()
                .incremental(true).x(0).y(0).width(800).height(600).build();
        byte[] bytes = serialize(msg::write);
        ClientMessage result = ClientMessageDispatch.read(streamOf(bytes));
        assertInstanceOf(FramebufferUpdateRequest.class, result);
        assertEquals(800, ((FramebufferUpdateRequest) result).width());
    }

    @Test
    void testClientDispatch_keyEvent() throws IOException {
        KeyEvent msg = KeyEvent.newBuilder().down(true).key(0x61).build();
        byte[] bytes = serialize(msg::write);
        ClientMessage result = ClientMessageDispatch.read(streamOf(bytes));
        assertInstanceOf(KeyEvent.class, result);
        assertEquals(0x61, ((KeyEvent) result).key());
    }

    @Test
    void testClientDispatch_pointerEvent() throws IOException {
        PointerEvent msg = PointerEvent.newBuilder().buttonMask(1).x(100).y(200).build();
        byte[] bytes = serialize(msg::write);
        ClientMessage result = ClientMessageDispatch.read(streamOf(bytes));
        assertInstanceOf(PointerEvent.class, result);
        assertEquals(100, ((PointerEvent) result).x());
    }

    @Test
    void testClientDispatch_clientCutText() throws IOException {
        ClientCutText msg = ClientCutText.newBuilder()
                .text("hello".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1)).build();
        byte[] bytes = serialize(msg::write);
        ClientMessage result = ClientMessageDispatch.read(streamOf(bytes));
        assertInstanceOf(ClientCutText.class, result);
        assertArrayEquals("hello".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1),
                ((ClientCutText) result).text());
    }

    @Test
    void testClientDispatch_unknownType_throws() {
        byte[] bytes = new byte[]{(byte) 99}; // unknown type
        assertThrows(UnsupportedOperationException.class,
                () -> ClientMessageDispatch.read(streamOf(bytes)));
    }

    @Test
    void testClientDispatch_eof_throws() {
        byte[] bytes = new byte[0];
        assertThrows(java.io.EOFException.class,
                () -> ClientMessageDispatch.read(streamOf(bytes)));
    }

    // -----------------------------------------------------------------------
    // ServerMessageDispatch
    // -----------------------------------------------------------------------

    @Test
    void testServerDispatch_framebufferUpdate() throws IOException {
        // FramebufferUpdate with one Raw rectangle (1x1, 1bpp effectively)
        PixelFormat pf8 = PixelFormat.newBuilder()
                .bitsPerPixel(8).depth(8).bigEndian(false).trueColour(false)
                .redMax(7).greenMax(7).blueMax(3)
                .redShift(5).greenShift(2).blueShift(0).build();
        RfbRectangleRaw raw = RfbRectangleRaw.newBuilder()
                .x(0).y(0).width(1).height(1)
                .pixels(new byte[]{0x42}).build();
        FramebufferUpdate msg = FramebufferUpdate.newBuilder()
                .rectangles(List.of(raw)).build();
        byte[] bytes = serialize(msg::write);
        // ServerMessageDispatch reads the type byte (0) + padding + count + rectangles
        ServerMessage result = ServerMessageDispatch.read(streamOf(bytes), pf8);
        assertInstanceOf(FramebufferUpdate.class, result);
        assertEquals(1, ((FramebufferUpdate) result).rectangles().size());
    }

    @Test
    void testServerDispatch_setColourMapEntries() throws IOException {
        ColourMapEntry entry = ColourMapEntry.newBuilder().red(0xFFFF).green(0).blue(0).build();
        SetColourMapEntries msg = SetColourMapEntries.newBuilder()
                .firstColour(0).colours(List.of(entry)).build();
        byte[] bytes = serialize(msg::write);
        ServerMessage result = ServerMessageDispatch.read(streamOf(bytes), PIXEL_FORMAT);
        assertInstanceOf(SetColourMapEntries.class, result);
        assertEquals(1, ((SetColourMapEntries) result).colours().size());
    }

    @Test
    void testServerDispatch_bell() throws IOException {
        Bell msg = Bell.newBuilder().build();
        byte[] bytes = serialize(msg::write);
        ServerMessage result = ServerMessageDispatch.read(streamOf(bytes), PIXEL_FORMAT);
        assertInstanceOf(Bell.class, result);
    }

    @Test
    void testServerDispatch_serverCutText() throws IOException {
        ServerCutText msg = ServerCutText.newBuilder()
                .text("clipboard".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1)).build();
        byte[] bytes = serialize(msg::write);
        ServerMessage result = ServerMessageDispatch.read(streamOf(bytes), PIXEL_FORMAT);
        assertInstanceOf(ServerCutText.class, result);
    }

    @Test
    void testServerDispatch_unknownType_throws() {
        byte[] bytes = new byte[]{(byte) 99};
        assertThrows(UnsupportedOperationException.class,
                () -> ServerMessageDispatch.read(streamOf(bytes), PIXEL_FORMAT));
    }

    @Test
    void testServerDispatch_eof_throws() {
        byte[] bytes = new byte[0];
        assertThrows(java.io.EOFException.class,
                () -> ServerMessageDispatch.read(streamOf(bytes), PIXEL_FORMAT));
    }

    // -----------------------------------------------------------------------
    // RfbRectangleDispatch
    // -----------------------------------------------------------------------

    /**
     * Verifies that RfbRectangleDispatch routes Raw (encoding=0) to RfbRectangleRaw.
     *
     * <p>Rectangle wire format: U16 x, U16 y, U16 width, U16 height, S32 encodingType,
     * then the encoding-specific payload.
     */
    @Test
    void testRectangleDispatch_raw() throws IOException {
        PixelFormat pf = PixelFormat.newBuilder()
                .bitsPerPixel(8).depth(8).bigEndian(false).trueColour(false)
                .redMax(7).greenMax(7).blueMax(3)
                .redShift(5).greenShift(2).blueShift(0).build();
        RfbRectangleRaw msg = RfbRectangleRaw.newBuilder()
                .x(10).y(20).width(1).height(1)
                .pixels(new byte[]{0x5A}).build();
        byte[] bytes = serialize(msg::write);
        RfbRectangle result = RfbRectangleDispatch.read(streamOf(bytes), pf);
        assertInstanceOf(RfbRectangleRaw.class, result);
        assertEquals(10, result.x());
        assertEquals(20, result.y());
    }

    @Test
    void testRectangleDispatch_copyRect() throws IOException {
        RfbRectangleCopyRect msg = RfbRectangleCopyRect.newBuilder()
                .x(0).y(0).width(10).height(10)
                .srcX(5).srcY(5).build();
        byte[] bytes = serialize(msg::write);
        RfbRectangle result = RfbRectangleDispatch.read(streamOf(bytes), PIXEL_FORMAT);
        assertInstanceOf(RfbRectangleCopyRect.class, result);
        assertEquals(5, ((RfbRectangleCopyRect) result).srcX());
    }

    @Test
    void testRectangleDispatch_desktopSize() throws IOException {
        RfbRectangleDesktopSize msg = RfbRectangleDesktopSize.newBuilder()
                .x(0).y(0).width(1280).height(720).build();
        byte[] bytes = serialize(msg::write);
        RfbRectangle result = RfbRectangleDispatch.read(streamOf(bytes), PIXEL_FORMAT);
        assertInstanceOf(RfbRectangleDesktopSize.class, result);
        assertEquals(1280, result.width());
        assertEquals(720, result.height());
    }

    @Test
    void testRectangleDispatch_lastRect() throws IOException {
        RfbRectangleLastRect msg = RfbRectangleLastRect.newBuilder()
                .x(0).y(0).width(0).height(0).build();
        byte[] bytes = serialize(msg::write);
        RfbRectangle result = RfbRectangleDispatch.read(streamOf(bytes), PIXEL_FORMAT);
        assertInstanceOf(RfbRectangleLastRect.class, result);
    }

    @Test
    void testRectangleDispatch_zlib() throws IOException {
        RfbRectangleZlib msg = RfbRectangleZlib.newBuilder()
                .x(0).y(0).width(4).height(4)
                .zlibData(new byte[]{1, 2, 3, 4}).build();
        byte[] bytes = serialize(msg::write);
        RfbRectangle result = RfbRectangleDispatch.read(streamOf(bytes), PIXEL_FORMAT);
        assertInstanceOf(RfbRectangleZlib.class, result);
        assertArrayEquals(new byte[]{1, 2, 3, 4}, ((RfbRectangleZlib) result).zlibData());
    }

    @Test
    void testRectangleDispatch_zrle() throws IOException {
        RfbRectangleZrle msg = RfbRectangleZrle.newBuilder()
                .x(0).y(0).width(4).height(4)
                .zlibData(new byte[]{0xA, 0xB}).build();
        byte[] bytes = serialize(msg::write);
        RfbRectangle result = RfbRectangleDispatch.read(streamOf(bytes), PIXEL_FORMAT);
        assertInstanceOf(RfbRectangleZrle.class, result);
    }

    @Test
    void testRectangleDispatch_h264() throws IOException {
        RfbRectangleH264 msg = RfbRectangleH264.newBuilder()
                .x(0).y(0).width(16).height(16)
                .flags(0).data(new byte[]{0x00, 0x01, 0x02}).build();
        byte[] bytes = serialize(msg::write);
        RfbRectangle result = RfbRectangleDispatch.read(streamOf(bytes), PIXEL_FORMAT);
        assertInstanceOf(RfbRectangleH264.class, result);
        assertArrayEquals(new byte[]{0x00, 0x01, 0x02}, ((RfbRectangleH264) result).data());
    }

    @Test
    void testRectangleDispatch_extendedDesktopSize() throws IOException {
        Screen screen = Screen.newBuilder().id(1).x(0).y(0).width(1920).height(1080).flags(0).build();
        RfbRectangleExtendedDesktopSize msg = RfbRectangleExtendedDesktopSize.newBuilder()
                .x(0).y(0).width(1920).height(1080)
                .screens(List.of(screen)).build();
        byte[] bytes = serialize(msg::write);
        RfbRectangle result = RfbRectangleDispatch.read(streamOf(bytes), PIXEL_FORMAT);
        assertInstanceOf(RfbRectangleExtendedDesktopSize.class, result);
        assertEquals(1, ((RfbRectangleExtendedDesktopSize) result).screens().size());
    }

    @Test
    void testRectangleDispatch_tightFill() throws IOException {
        // Tight (encoding=7), ctrl=0x80 (FillCompression)
        // TPIXEL for PIXEL_FORMAT (bpp=32, depth=24, true-colour, RGB8) is 3 bytes
        RfbRectangleTightFill msg = RfbRectangleTightFill.newBuilder()
                .x(0).y(0).width(4).height(4)
                .streamResets(0).fillColor(new byte[]{(byte)0xFF, 0x00, 0x00}).build();
        byte[] bytes = serialize(msg::write);
        RfbRectangle result = RfbRectangleDispatch.read(streamOf(bytes), PIXEL_FORMAT);
        assertInstanceOf(RfbRectangleTightFill.class, result);
        assertArrayEquals(new byte[]{(byte)0xFF, 0x00, 0x00},
                ((RfbRectangleTightFill) result).fillColor());
    }

    @Test
    void testRectangleDispatch_tightJpeg() throws IOException {
        RfbRectangleTightJpeg msg = RfbRectangleTightJpeg.newBuilder()
                .x(0).y(0).width(4).height(4)
                .streamResets(0).jpegData(new byte[]{(byte)0xFF, (byte)0xD8, 0x01}).build();
        byte[] bytes = serialize(msg::write);
        RfbRectangle result = RfbRectangleDispatch.read(streamOf(bytes), PIXEL_FORMAT);
        assertInstanceOf(RfbRectangleTightJpeg.class, result);
        assertArrayEquals(new byte[]{(byte)0xFF, (byte)0xD8, 0x01},
                ((RfbRectangleTightJpeg) result).jpegData());
    }

    @Test
    void testRectangleDispatch_unknownType_throws() {
        // Build a 12-byte header with unknown encoding type = 999
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);
        try {
            dos.writeShort(0); // x
            dos.writeShort(0); // y
            dos.writeShort(4); // w
            dos.writeShort(4); // h
            dos.writeInt(999); // unknown encoding
        } catch (IOException e) {
            fail("Setup failed: " + e.getMessage());
        }
        assertThrows(UnsupportedOperationException.class,
                () -> RfbRectangleDispatch.read(streamOf(baos.toByteArray()), PIXEL_FORMAT));
    }

    @Test
    void testRectangleDispatch_jpegEncoding_throws() {
        // JPEG encoding (type=21) throws UnsupportedOperationException
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);
        try {
            dos.writeShort(0); dos.writeShort(0); dos.writeShort(4); dos.writeShort(4);
            dos.writeInt(21); // JPEG encoding
        } catch (IOException e) {
            fail("Setup failed: " + e.getMessage());
        }
        assertThrows(UnsupportedOperationException.class,
                () -> RfbRectangleDispatch.read(streamOf(baos.toByteArray()), PIXEL_FORMAT));
    }

    // -----------------------------------------------------------------------
    // GiiEventDispatch
    // -----------------------------------------------------------------------

    /**
     * Verifies that GiiEventDispatch routes eventType=5 (key press) to GiiKeyEvent.
     *
     * <p>GII event wire format: U8 event-size, U8 event-type, then type-specific fields.
     * Key events are types 5 (key-press), 6 (key-release), 7 (key-repeat).
     * Pointer move events are types 8, 9.
     * Pointer button events are types 10 (press), 11 (release).
     * Valuator events are types 12 (relative), 13 (absolute).
     */
    @Test
    void testGiiEventDispatch_keyEvent() throws IOException {
        GiiKeyEvent event = GiiKeyEvent.newBuilder()
                .eventType(5).deviceOrigin(1L).modifiers(0L)
                .symbol(0x61L).label(0L).button(0L).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        event.write(baos, true);
        GiiEvent result = GiiEventDispatch.readEvent(streamOf(baos.toByteArray()), true);
        assertInstanceOf(GiiKeyEvent.class, result);
        assertEquals(0x61L, ((GiiKeyEvent) result).symbol());
    }

    @Test
    void testGiiEventDispatch_pointerMoveEvent() throws IOException {
        GiiPointerMoveEvent event = GiiPointerMoveEvent.newBuilder()
                .eventType(8).deviceOrigin(2L)
                .x(100).y(200).z(0).wheel(0).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        event.write(baos, true);
        GiiEvent result = GiiEventDispatch.readEvent(streamOf(baos.toByteArray()), true);
        assertInstanceOf(GiiPointerMoveEvent.class, result);
    }

    @Test
    void testGiiEventDispatch_pointerButtonEvent() throws IOException {
        GiiPointerButtonEvent event = GiiPointerButtonEvent.newBuilder()
                .eventType(10).deviceOrigin(1L).buttonNumber(1L).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        event.write(baos, true);
        GiiEvent result = GiiEventDispatch.readEvent(streamOf(baos.toByteArray()), true);
        assertInstanceOf(GiiPointerButtonEvent.class, result);
        assertEquals(1L, ((GiiPointerButtonEvent) result).buttonNumber());
    }

    @Test
    void testGiiEventDispatch_valuatorEvent() throws IOException {
        GiiValuatorEvent event = GiiValuatorEvent.newBuilder()
                .eventType(13).deviceOrigin(1L)
                .first(0L).values(List.of(512)).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        event.write(baos, true);
        GiiEvent result = GiiEventDispatch.readEvent(streamOf(baos.toByteArray()), true);
        assertInstanceOf(GiiValuatorEvent.class, result);
    }

    @Test
    void testGiiEventDispatch_unknownType_returnsNull() throws IOException {
        // Unknown event type with event-size=4 (2 header + 2 unknown data)
        byte[] bytes = {4, (byte) 99, 0, 0}; // size=4, type=99, 2 padding bytes
        GiiEvent result = GiiEventDispatch.readEvent(streamOf(bytes), true);
        assertNull(result);
    }

    // -----------------------------------------------------------------------
    // TightCapability
    // -----------------------------------------------------------------------

    /**
     * Verifies that TightCapability can be built and its fields are accessible.
     */
    @Test
    void testTightCapability_build() {
        TightCapability cap = TightCapability.newBuilder()
                .code(1).vendor("TIGHT").signature("JPEGOPT-").build();
        assertEquals(1, cap.code());
        assertEquals("TIGHT", cap.vendor());
        assertEquals("JPEGOPT-", cap.signature());
    }

    @Test
    void testTightCapability_equality() {
        TightCapability cap1 = TightCapability.newBuilder()
                .code(1).vendor("TIGHT").signature("JPEGOPT-").build();
        TightCapability cap2 = TightCapability.newBuilder()
                .code(1).vendor("TIGHT").signature("JPEGOPT-").build();
        assertEquals(cap1, cap2);
        assertEquals(cap1.hashCode(), cap2.hashCode());
    }

    @Test
    void testTightCapability_toString() {
        TightCapability cap = TightCapability.newBuilder()
                .code(42).vendor("VEND").signature("SIGNAT--").build();
        String str = cap.toString();
        assertNotNull(str);
        assertFalse(str.isEmpty());
    }

    @Test
    void testTightCapability_fromBuilder() {
        TightCapability original = TightCapability.newBuilder()
                .code(7).vendor("MYVEND").signature("MYSIG---").build();
        TightCapability copy = TightCapability.newBuilder().from(original).build();
        assertEquals(original.code(), copy.code());
        assertEquals(original.vendor(), copy.vendor());
        assertEquals(original.signature(), copy.signature());
    }
}
