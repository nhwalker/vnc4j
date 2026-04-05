package io.github.nhwalker.vnc4j.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Read round-trip and equality tests for handshake messages and GiiInjectEvents.
 *
 * <p>From rfbproto.rst.txt – SecurityResult:
 * <pre>
 * status : U32  (0=OK, 1=Failed, 2=Failed-too-many)
 * If status != 0:
 *   reasonLength : U32
 *   reason       : U8 array[reasonLength]  (UTF-8)
 * </pre>
 *
 * <p>From rfbproto.rst.txt – ServerInit:
 * <pre>
 * framebufferWidth  : U16
 * framebufferHeight : U16
 * pixelFormat       : 16 bytes
 * nameLength        : U32
 * nameString        : U8 array[nameLength]  (UTF-8)
 * </pre>
 *
 * <p>GiiInjectEvents (type=253, sub=0x80 or 0x00):
 * <pre>
 * messageType     : U8 = 253
 * endianAndSubType: U8 (0x80 = big-endian, 0x00 = little-endian)
 * length          : EU16 (in client byte order)
 * events          : &lt;length bytes of packed GII event records&gt;
 * </pre>
 */
class HandshakeAndGiiReadTest {

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
    // SecurityResult
    // -----------------------------------------------------------------------

    @Test
    void testSecurityResult_readRoundTrip_success() throws IOException {
        SecurityResult orig = SecurityResult.newBuilder()
                .status(0).failureReason(null).build();
        byte[] bytes = serialize(orig::write);
        SecurityResult copy = SecurityResult.read(streamOf(bytes));
        assertEquals(0, copy.status());
        assertNull(copy.failureReason());
        assertEquals(orig, copy);
    }

    @Test
    void testSecurityResult_readRoundTrip_failure() throws IOException {
        SecurityResult orig = SecurityResult.newBuilder()
                .status(1).failureReason("Authentication failed").build();
        byte[] bytes = serialize(orig::write);
        SecurityResult copy = SecurityResult.read(streamOf(bytes));
        assertEquals(1, copy.status());
        assertEquals("Authentication failed", copy.failureReason());
        assertEquals(orig, copy);
    }

    @Test
    void testSecurityResult_readRoundTrip_tooManyAttempts() throws IOException {
        SecurityResult orig = SecurityResult.newBuilder()
                .status(2).failureReason("Too many attempts").build();
        byte[] bytes = serialize(orig::write);
        SecurityResult copy = SecurityResult.read(streamOf(bytes));
        assertEquals(2, copy.status());
        assertEquals("Too many attempts", copy.failureReason());
    }

    @Test
    void testSecurityResult_equals() {
        SecurityResult a = SecurityResult.newBuilder().status(0).failureReason(null).build();
        SecurityResult b = SecurityResult.newBuilder().status(0).failureReason(null).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testSecurityResult_notEquals() {
        SecurityResult a = SecurityResult.newBuilder().status(0).failureReason(null).build();
        SecurityResult b = SecurityResult.newBuilder().status(1).failureReason("error").build();
        assertNotEquals(a, b);
    }

    @Test
    void testSecurityResult_toString() {
        SecurityResult r = SecurityResult.newBuilder().status(1).failureReason("oops").build();
        assertNotNull(r.toString());
    }

    @Test
    void testSecurityResult_fromBuilder() {
        SecurityResult orig = SecurityResult.newBuilder().status(1).failureReason("denied").build();
        SecurityResult copy = SecurityResult.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    // -----------------------------------------------------------------------
    // ServerInit
    // -----------------------------------------------------------------------

    @Test
    void testServerInit_readRoundTrip() throws IOException {
        ServerInit orig = ServerInit.newBuilder()
                .framebufferWidth(1920).framebufferHeight(1080)
                .pixelFormat(PF_32BPP).name("My Desktop").build();
        byte[] bytes = serialize(orig::write);
        ServerInit copy = ServerInit.read(streamOf(bytes));
        assertEquals(1920, copy.framebufferWidth());
        assertEquals(1080, copy.framebufferHeight());
        assertEquals("My Desktop", copy.name());
        assertEquals(orig, copy);
    }

    @Test
    void testServerInit_equals() {
        ServerInit a = ServerInit.newBuilder()
                .framebufferWidth(800).framebufferHeight(600)
                .pixelFormat(PF_32BPP).name("Desktop").build();
        ServerInit b = ServerInit.newBuilder()
                .framebufferWidth(800).framebufferHeight(600)
                .pixelFormat(PF_32BPP).name("Desktop").build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testServerInit_notEquals_differentName() {
        ServerInit a = ServerInit.newBuilder()
                .framebufferWidth(800).framebufferHeight(600)
                .pixelFormat(PF_32BPP).name("Screen A").build();
        ServerInit b = ServerInit.newBuilder()
                .framebufferWidth(800).framebufferHeight(600)
                .pixelFormat(PF_32BPP).name("Screen B").build();
        assertNotEquals(a, b);
    }

    @Test
    void testServerInit_toString() {
        ServerInit s = ServerInit.newBuilder()
                .framebufferWidth(1024).framebufferHeight(768)
                .pixelFormat(PF_32BPP).name("Test").build();
        assertNotNull(s.toString());
    }

    @Test
    void testServerInit_fromBuilder() {
        ServerInit orig = ServerInit.newBuilder()
                .framebufferWidth(640).framebufferHeight(480)
                .pixelFormat(PF_32BPP).name("VNC Session").build();
        ServerInit copy = ServerInit.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    // -----------------------------------------------------------------------
    // GiiInjectEvents read round-trips
    // -----------------------------------------------------------------------

    @Test
    void testGiiInjectEvents_readRoundTrip_bigEndian_keyEvent() throws IOException {
        GiiKeyEvent keyEvent = GiiKeyEvent.newBuilder()
                .eventType(5).deviceOrigin(1L).modifiers(0L)
                .symbol(0x41L).label(0L).button(0L).build();
        GiiInjectEvents orig = GiiInjectEvents.newBuilder()
                .bigEndian(true).events(List.of(keyEvent)).build();
        byte[] bytes = serialize(orig::write);
        // GiiInjectEvents.read() expects the stream AFTER the type byte (type=253)
        // The write() includes type byte at index 0; skip it
        InputStream in = new ByteArrayInputStream(bytes, 1, bytes.length - 1);
        GiiInjectEvents copy = GiiInjectEvents.read(in);
        assertTrue(copy.bigEndian());
        assertEquals(1, copy.events().size());
        assertInstanceOf(GiiKeyEvent.class, copy.events().get(0));
        assertEquals(0x41L, ((GiiKeyEvent) copy.events().get(0)).symbol());
    }

    @Test
    void testGiiInjectEvents_readRoundTrip_littleEndian_pointerButton() throws IOException {
        GiiPointerButtonEvent buttonEvent = GiiPointerButtonEvent.newBuilder()
                .eventType(10).deviceOrigin(2L).buttonNumber(1L).build();
        GiiInjectEvents orig = GiiInjectEvents.newBuilder()
                .bigEndian(false).events(List.of(buttonEvent)).build();
        byte[] bytes = serialize(orig::write);
        InputStream in = new ByteArrayInputStream(bytes, 1, bytes.length - 1);
        GiiInjectEvents copy = GiiInjectEvents.read(in);
        assertFalse(copy.bigEndian());
        assertEquals(1, copy.events().size());
        assertInstanceOf(GiiPointerButtonEvent.class, copy.events().get(0));
    }

    @Test
    void testGiiInjectEvents_readRoundTrip_multipleEvents() throws IOException {
        GiiKeyEvent keyDown = GiiKeyEvent.newBuilder()
                .eventType(5).deviceOrigin(1L).modifiers(0L)
                .symbol(0x41L).label(0L).button(0L).build();
        GiiKeyEvent keyUp = GiiKeyEvent.newBuilder()
                .eventType(6).deviceOrigin(1L).modifiers(0L)
                .symbol(0x41L).label(0L).button(0L).build();
        GiiInjectEvents orig = GiiInjectEvents.newBuilder()
                .bigEndian(true).events(List.of(keyDown, keyUp)).build();
        byte[] bytes = serialize(orig::write);
        InputStream in = new ByteArrayInputStream(bytes, 1, bytes.length - 1);
        GiiInjectEvents copy = GiiInjectEvents.read(in);
        assertEquals(2, copy.events().size());
    }

    @Test
    void testGiiInjectEvents_equals() {
        GiiKeyEvent event = GiiKeyEvent.newBuilder()
                .eventType(5).deviceOrigin(1L).modifiers(0L)
                .symbol(0x20L).label(0L).button(0L).build();
        GiiInjectEvents a = GiiInjectEvents.newBuilder()
                .bigEndian(true).events(List.of(event)).build();
        GiiInjectEvents b = GiiInjectEvents.newBuilder()
                .bigEndian(true).events(List.of(event)).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testGiiInjectEvents_toString() {
        GiiInjectEvents g = GiiInjectEvents.newBuilder()
                .bigEndian(false).events(List.of()).build();
        assertNotNull(g.toString());
    }

    @Test
    void testGiiInjectEvents_fromBuilder() {
        GiiInjectEvents orig = GiiInjectEvents.newBuilder()
                .bigEndian(true).events(List.of()).build();
        GiiInjectEvents copy = GiiInjectEvents.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    // -----------------------------------------------------------------------
    // TightBasic / TightJpeg with 3-byte compact-length (length >= 16384)
    // to exercise TightIo.writeCompactLength()/readCompactLength() 3-byte path
    // -----------------------------------------------------------------------

    /**
     * The Tight compact-length encoding uses 3 bytes when length >= 16384.
     * <pre>
     * byte0 = (length & 0x7F) | 0x80
     * byte1 = ((length >> 7) & 0x7F) | 0x80
     * byte2 = (length >> 14) & 0xFF
     * </pre>
     */
    @Test
    void testTightJpeg_readRoundTrip_3ByteCompactLength() throws IOException {
        // 16384 bytes: first 3-byte compact-length case
        byte[] jpegData = new byte[16384];
        for (int i = 0; i < jpegData.length; i++) jpegData[i] = (byte)(i & 0xFF);
        RfbRectangleTightJpeg orig = RfbRectangleTightJpeg.newBuilder()
                .x(0).y(0).width(128).height(128)
                .streamResets(0).jpegData(jpegData).build();
        byte[] bytes = serialize(orig::write);
        RfbRectangle result = RfbRectangle.read(streamOf(bytes), PF_32BPP);
        assertInstanceOf(RfbRectangleTightJpeg.class, result);
        assertArrayEquals(jpegData, ((RfbRectangleTightJpeg) result).jpegData());
    }

    /**
     * TightFill read with 8bpp non-standard pixel format exercises the
     * tpixelSize() fallback path (bitsPerPixel / 8 = 1 byte).
     */
    @Test
    void testTightFill_readRoundTrip_8bpp() throws IOException {
        PixelFormat pf8 = PixelFormat.newBuilder()
                .bitsPerPixel(8).depth(8).bigEndian(false).trueColour(false)
                .redMax(7).greenMax(7).blueMax(3)
                .redShift(5).greenShift(2).blueShift(0).build();
        RfbRectangleTightFill orig = RfbRectangleTightFill.newBuilder()
                .x(0).y(0).width(4).height(4)
                .streamResets(0).fillColor(new byte[]{0x42}).build();
        byte[] bytes = serialize(orig::write);
        RfbRectangle result = RfbRectangle.read(streamOf(bytes), pf8);
        assertInstanceOf(RfbRectangleTightFill.class, result);
        assertArrayEquals(new byte[]{0x42}, ((RfbRectangleTightFill) result).fillColor());
    }

    /**
     * TightPngFill read with 8bpp pixel format also exercises tpixelSize() fallback.
     */
    @Test
    void testTightPngFill_readRoundTrip_8bpp() throws IOException {
        PixelFormat pf8 = PixelFormat.newBuilder()
                .bitsPerPixel(8).depth(8).bigEndian(false).trueColour(false)
                .redMax(7).greenMax(7).blueMax(3)
                .redShift(5).greenShift(2).blueShift(0).build();
        RfbRectangleTightPngFill orig = RfbRectangleTightPngFill.newBuilder()
                .x(0).y(0).width(4).height(4)
                .streamResets(0).fillColor(new byte[]{0x7F}).build();
        byte[] bytes = serialize(orig::write);
        RfbRectangle result = RfbRectangle.read(streamOf(bytes), pf8);
        assertInstanceOf(RfbRectangleTightPngFill.class, result);
        assertArrayEquals(new byte[]{0x7F}, ((RfbRectangleTightPngFill) result).fillColor());
    }

    /**
     * TightBasic GradientFilter read round-trip.
     * GradientFilter (filterType=2): ctrl has ReadFilter (0x40) set, filter-byte=2,
     * then compact-length + compressed data.
     */
    @Test
    void testTightBasic_readRoundTrip_gradientFilter() throws IOException {
        RfbRectangleTightBasic orig = RfbRectangleTightBasic.newBuilder()
                .x(0).y(0).width(4).height(4)
                .streamResets(0).streamNumber(0)
                .filterType(RfbRectangleTightBasic.FILTER_GRADIENT).paletteSize(0)
                .palette(null).compressedData(new byte[]{0x78, (byte)0x9C, 0x05}).build();
        byte[] bytes = serialize(orig::write);
        RfbRectangle result = RfbRectangle.read(streamOf(bytes), PF_32BPP);
        assertInstanceOf(RfbRectangleTightBasic.class, result);
        RfbRectangleTightBasic copy = (RfbRectangleTightBasic) result;
        assertEquals(RfbRectangleTightBasic.FILTER_GRADIENT, copy.filterType());
    }
}
