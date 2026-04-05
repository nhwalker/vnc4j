package io.github.nhwalker.vnc4j.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for equals/hashCode/toString/read and direct static-read methods for
 * singleton-style messages and other message types with gaps in coverage.
 *
 * <p>Covers: Bell, EndOfContinuousUpdates, QemuAudioServerEnd, QemuAudioServerBegin,
 * QemuAudioClientEnable, QemuAudioClientDisable, QemuAudioServerData,
 * ClientFence, ServerFence, ClientCutText, ServerCutText, and GiiEvent subtypes.
 */
class MessageEqualityAndReadTest {

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
    // Bell (singleton)
    // -----------------------------------------------------------------------

    /**
     * Bell message: just a single type byte (2) on the wire.
     * Since it has no fields, any two Bell instances are equal.
     */
    @Test
    void testBell_readRoundTrip() throws IOException {
        Bell orig = Bell.newBuilder().build();
        byte[] bytes = serialize(orig::write);
        // Bell.read() expects the stream AFTER the type byte (type=2 consumed by dispatcher)
        Bell copy = Bell.read(streamOf(new byte[0]));
        assertEquals(orig, copy);
    }

    @Test
    void testBell_equals_otherBell() {
        Bell a = Bell.newBuilder().build();
        Bell b = Bell.newBuilder().build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testBell_notEquals_nonBell() {
        Bell bell = Bell.newBuilder().build();
        assertNotEquals(bell, new Object());
        assertNotEquals(bell, null);
    }

    @Test
    void testBell_toString() {
        Bell bell = Bell.newBuilder().build();
        assertNotNull(bell.toString());
        assertFalse(bell.toString().isEmpty());
    }

    // -----------------------------------------------------------------------
    // EndOfContinuousUpdates (singleton)
    // -----------------------------------------------------------------------

    @Test
    void testEndOfContinuousUpdates_readRoundTrip() throws IOException {
        EndOfContinuousUpdates orig = EndOfContinuousUpdates.newBuilder().build();
        // read() is called after type byte; no additional bytes to read
        EndOfContinuousUpdates copy = EndOfContinuousUpdates.read(streamOf(new byte[0]));
        assertEquals(orig, copy);
    }

    @Test
    void testEndOfContinuousUpdates_equals() {
        EndOfContinuousUpdates a = EndOfContinuousUpdates.newBuilder().build();
        EndOfContinuousUpdates b = EndOfContinuousUpdates.newBuilder().build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testEndOfContinuousUpdates_notEquals_nonInstance() {
        EndOfContinuousUpdates ecu = EndOfContinuousUpdates.newBuilder().build();
        assertNotEquals(ecu, new Object());
        assertNotEquals(ecu, null);
    }

    @Test
    void testEndOfContinuousUpdates_toString() {
        EndOfContinuousUpdates ecu = EndOfContinuousUpdates.newBuilder().build();
        assertNotNull(ecu.toString());
    }

    // -----------------------------------------------------------------------
    // QemuAudioServerEnd (singleton)
    // -----------------------------------------------------------------------

    @Test
    void testQemuAudioServerEnd_readRoundTrip() throws IOException {
        QemuAudioServerEnd orig = QemuAudioServerEnd.newBuilder().build();
        QemuAudioServerEnd copy = QemuAudioServerEnd.read(streamOf(new byte[0]));
        assertEquals(orig, copy);
    }

    @Test
    void testQemuAudioServerEnd_equals() {
        QemuAudioServerEnd a = QemuAudioServerEnd.newBuilder().build();
        QemuAudioServerEnd b = QemuAudioServerEnd.newBuilder().build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testQemuAudioServerEnd_notEquals_nonInstance() {
        QemuAudioServerEnd x = QemuAudioServerEnd.newBuilder().build();
        assertNotEquals(x, new Object());
    }

    @Test
    void testQemuAudioServerEnd_toString() {
        assertNotNull(QemuAudioServerEnd.newBuilder().build().toString());
    }

    // -----------------------------------------------------------------------
    // QemuAudioServerBegin (singleton)
    // -----------------------------------------------------------------------

    @Test
    void testQemuAudioServerBegin_readRoundTrip() throws IOException {
        QemuAudioServerBegin orig = QemuAudioServerBegin.newBuilder().build();
        QemuAudioServerBegin copy = QemuAudioServerBegin.read(streamOf(new byte[0]));
        assertEquals(orig, copy);
    }

    @Test
    void testQemuAudioServerBegin_equals() {
        QemuAudioServerBegin a = QemuAudioServerBegin.newBuilder().build();
        QemuAudioServerBegin b = QemuAudioServerBegin.newBuilder().build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testQemuAudioServerBegin_notEquals_nonInstance() {
        assertNotEquals(QemuAudioServerBegin.newBuilder().build(), "not a begin");
    }

    @Test
    void testQemuAudioServerBegin_toString() {
        assertNotNull(QemuAudioServerBegin.newBuilder().build().toString());
    }

    // -----------------------------------------------------------------------
    // QemuAudioClientEnable (singleton)
    // -----------------------------------------------------------------------

    @Test
    void testQemuAudioClientEnable_readRoundTrip() throws IOException {
        QemuAudioClientEnable orig = QemuAudioClientEnable.newBuilder().build();
        QemuAudioClientEnable copy = QemuAudioClientEnable.read(streamOf(new byte[0]));
        assertEquals(orig, copy);
    }

    @Test
    void testQemuAudioClientEnable_equals() {
        QemuAudioClientEnable a = QemuAudioClientEnable.newBuilder().build();
        QemuAudioClientEnable b = QemuAudioClientEnable.newBuilder().build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testQemuAudioClientEnable_notEquals_nonInstance() {
        assertNotEquals(QemuAudioClientEnable.newBuilder().build(), new Object());
    }

    @Test
    void testQemuAudioClientEnable_toString() {
        assertNotNull(QemuAudioClientEnable.newBuilder().build().toString());
    }

    // -----------------------------------------------------------------------
    // QemuAudioClientDisable (singleton)
    // -----------------------------------------------------------------------

    @Test
    void testQemuAudioClientDisable_readRoundTrip() throws IOException {
        QemuAudioClientDisable orig = QemuAudioClientDisable.newBuilder().build();
        QemuAudioClientDisable copy = QemuAudioClientDisable.read(streamOf(new byte[0]));
        assertEquals(orig, copy);
    }

    @Test
    void testQemuAudioClientDisable_equals() {
        QemuAudioClientDisable a = QemuAudioClientDisable.newBuilder().build();
        QemuAudioClientDisable b = QemuAudioClientDisable.newBuilder().build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testQemuAudioClientDisable_notEquals_nonInstance() {
        assertNotEquals(QemuAudioClientDisable.newBuilder().build(), "other");
    }

    @Test
    void testQemuAudioClientDisable_toString() {
        assertNotNull(QemuAudioClientDisable.newBuilder().build().toString());
    }

    // -----------------------------------------------------------------------
    // QemuAudioServerData
    // -----------------------------------------------------------------------

    /**
     * QemuAudioServerData.read() reads U32 length + data bytes.
     * The stream position is AFTER type byte (255), sub-type (1), and operation U16 (2).
     */
    @Test
    void testQemuAudioServerData_readRoundTrip() throws IOException {
        byte[] audioData = {0x10, 0x20, 0x30, 0x40};
        QemuAudioServerData orig = QemuAudioServerData.newBuilder().data(audioData).build();
        byte[] bytes = serialize(orig::write);
        // Skip type(1) + sub-type(1) + operation U16(2) = 4 bytes
        InputStream in = new ByteArrayInputStream(bytes, 4, bytes.length - 4);
        QemuAudioServerData copy = QemuAudioServerData.read(in);
        assertArrayEquals(audioData, copy.data());
        assertEquals(orig, copy);
    }

    @Test
    void testQemuAudioServerData_equals() {
        QemuAudioServerData a = QemuAudioServerData.newBuilder().data(new byte[]{1, 2}).build();
        QemuAudioServerData b = QemuAudioServerData.newBuilder().data(new byte[]{1, 2}).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testQemuAudioServerData_notEquals() {
        QemuAudioServerData a = QemuAudioServerData.newBuilder().data(new byte[]{1}).build();
        QemuAudioServerData b = QemuAudioServerData.newBuilder().data(new byte[]{2}).build();
        assertNotEquals(a, b);
    }

    @Test
    void testQemuAudioServerData_toString() {
        QemuAudioServerData d = QemuAudioServerData.newBuilder().data(new byte[]{0x55}).build();
        assertNotNull(d.toString());
    }

    @Test
    void testQemuAudioServerData_fromBuilder() {
        QemuAudioServerData orig = QemuAudioServerData.newBuilder().data(new byte[]{0x42}).build();
        QemuAudioServerData copy = QemuAudioServerData.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    // -----------------------------------------------------------------------
    // ClientFence read round-trip
    // -----------------------------------------------------------------------

    /**
     * ClientFence wire format (rfbproto.rst.txt):
     * <pre>
     * type     : U8 = 248
     * padding  : 3 bytes
     * flags    : U32
     * length   : U8
     * payload  : U8 array[length]
     * </pre>
     */
    @Test
    void testClientFence_readRoundTrip() throws IOException {
        ClientFence orig = ClientFence.newBuilder()
                .flags(0xDEADBEEF).payload(new byte[]{0x01, 0x02, 0x03}).build();
        byte[] bytes = serialize(orig::write);
        // ClientFence.read() expects stream after type byte (1 byte)
        InputStream in = new ByteArrayInputStream(bytes, 1, bytes.length - 1);
        ClientFence copy = ClientFence.read(in);
        assertEquals(0xDEADBEEF, copy.flags());
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03}, copy.payload());
        assertEquals(orig, copy);
    }

    @Test
    void testClientFence_equals() {
        ClientFence a = ClientFence.newBuilder().flags(1).payload(new byte[]{0x10}).build();
        ClientFence b = ClientFence.newBuilder().flags(1).payload(new byte[]{0x10}).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testClientFence_notEquals() {
        ClientFence a = ClientFence.newBuilder().flags(1).payload(new byte[]{0x01}).build();
        ClientFence b = ClientFence.newBuilder().flags(2).payload(new byte[]{0x01}).build();
        assertNotEquals(a, b);
    }

    @Test
    void testClientFence_toString() {
        ClientFence f = ClientFence.newBuilder().flags(0).payload(new byte[]{}).build();
        assertNotNull(f.toString());
    }

    @Test
    void testClientFence_fromBuilder() {
        ClientFence orig = ClientFence.newBuilder().flags(0xFF).payload(new byte[]{(byte)0xAB}).build();
        ClientFence copy = ClientFence.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    // -----------------------------------------------------------------------
    // ServerFence read round-trip
    // -----------------------------------------------------------------------

    /**
     * ServerFence (type=248) wire format is the same as ClientFence.
     */
    @Test
    void testServerFence_readRoundTrip() throws IOException {
        ServerFence orig = ServerFence.newBuilder()
                .flags(0x00000001).payload(new byte[]{(byte)0xAB}).build();
        byte[] bytes = serialize(orig::write);
        InputStream in = new ByteArrayInputStream(bytes, 1, bytes.length - 1);
        ServerFence copy = ServerFence.read(in);
        assertEquals(0x00000001, copy.flags());
        assertArrayEquals(new byte[]{(byte)0xAB}, copy.payload());
        assertEquals(orig, copy);
    }

    @Test
    void testServerFence_equals() {
        ServerFence a = ServerFence.newBuilder().flags(3).payload(new byte[]{}).build();
        ServerFence b = ServerFence.newBuilder().flags(3).payload(new byte[]{}).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testServerFence_notEquals() {
        ServerFence a = ServerFence.newBuilder().flags(1).payload(new byte[]{}).build();
        ServerFence b = ServerFence.newBuilder().flags(2).payload(new byte[]{}).build();
        assertNotEquals(a, b);
    }

    @Test
    void testServerFence_toString() {
        ServerFence f = ServerFence.newBuilder().flags(0).payload(new byte[0]).build();
        assertNotNull(f.toString());
    }

    @Test
    void testServerFence_fromBuilder() {
        ServerFence orig = ServerFence.newBuilder().flags(0x7F).payload(new byte[]{0x1, 0x2}).build();
        ServerFence copy = ServerFence.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    // -----------------------------------------------------------------------
    // ClientCutText / ServerCutText
    // -----------------------------------------------------------------------

    /**
     * ClientCutText (type=6) wire format:
     * <pre>
     * type    : U8 = 6
     * padding : 3 bytes
     * length  : U32
     * text    : U8 array[length]  (ISO 8859-1)
     * </pre>
     */
    @Test
    void testClientCutText_equals() {
        ClientCutText a = ClientCutText.newBuilder()
                .text("hello".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1)).build();
        ClientCutText b = ClientCutText.newBuilder()
                .text("hello".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1)).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testClientCutText_notEquals() {
        ClientCutText a = ClientCutText.newBuilder()
                .text("hello".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1)).build();
        ClientCutText b = ClientCutText.newBuilder()
                .text("world".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1)).build();
        assertNotEquals(a, b);
    }

    @Test
    void testClientCutText_toString() {
        ClientCutText c = ClientCutText.newBuilder()
                .text("test".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1)).build();
        assertNotNull(c.toString());
    }

    @Test
    void testClientCutText_fromBuilder() throws IOException {
        ClientCutText orig = ClientCutText.newBuilder()
                .text("copy".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1)).build();
        ClientCutText copy = ClientCutText.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    @Test
    void testServerCutText_equals() {
        ServerCutText a = ServerCutText.newBuilder()
                .text("server".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1)).build();
        ServerCutText b = ServerCutText.newBuilder()
                .text("server".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1)).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testServerCutText_notEquals() {
        ServerCutText a = ServerCutText.newBuilder()
                .text("abc".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1)).build();
        ServerCutText b = ServerCutText.newBuilder()
                .text("xyz".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1)).build();
        assertNotEquals(a, b);
    }

    @Test
    void testServerCutText_toString() {
        ServerCutText s = ServerCutText.newBuilder()
                .text("text".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1)).build();
        assertNotNull(s.toString());
    }

    @Test
    void testServerCutText_fromBuilder() {
        ServerCutText orig = ServerCutText.newBuilder()
                .text("paste".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1)).build();
        ServerCutText copy = ServerCutText.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    // -----------------------------------------------------------------------
    // GiiKeyEvent direct read (not via dispatcher)
    // -----------------------------------------------------------------------

    /**
     * GiiKeyEvent.read() is called AFTER event-size and event-type are consumed.
     * So the stream starts with the 2-byte padding (EU16), then 5 EU32 fields.
     */
    @Test
    void testGiiKeyEvent_directRead_bigEndian() throws IOException {
        GiiKeyEvent orig = GiiKeyEvent.newBuilder()
                .eventType(5).deviceOrigin(3L).modifiers(0L)
                .symbol(0x62L).label(0L).button(0L).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        orig.write(baos, true);
        // Skip event-size(1) + event-type(1) = 2 bytes
        InputStream in = new ByteArrayInputStream(baos.toByteArray(), 2, baos.toByteArray().length - 2);
        GiiKeyEvent copy = GiiKeyEvent.read(in, true);
        assertEquals(0x62L, copy.symbol());
        assertEquals(3L, copy.deviceOrigin());
    }

    @Test
    void testGiiKeyEvent_directRead_littleEndian() throws IOException {
        GiiKeyEvent orig = GiiKeyEvent.newBuilder()
                .eventType(6).deviceOrigin(1L).modifiers(0L)
                .symbol(0x63L).label(0L).button(0L).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        orig.write(baos, false);
        byte[] bytes = baos.toByteArray();
        InputStream in = new ByteArrayInputStream(bytes, 2, bytes.length - 2);
        GiiKeyEvent copy = GiiKeyEvent.read(in, false);
        assertEquals(0x63L, copy.symbol());
    }

    @Test
    void testGiiKeyEvent_equals() {
        GiiKeyEvent a = GiiKeyEvent.newBuilder()
                .eventType(5).deviceOrigin(1L).modifiers(0L)
                .symbol(0x41L).label(0L).button(0L).build();
        GiiKeyEvent b = GiiKeyEvent.newBuilder()
                .eventType(5).deviceOrigin(1L).modifiers(0L)
                .symbol(0x41L).label(0L).button(0L).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testGiiKeyEvent_notEquals() {
        GiiKeyEvent a = GiiKeyEvent.newBuilder()
                .eventType(5).deviceOrigin(1L).modifiers(0L)
                .symbol(0x41L).label(0L).button(0L).build();
        GiiKeyEvent b = GiiKeyEvent.newBuilder()
                .eventType(5).deviceOrigin(1L).modifiers(0L)
                .symbol(0x42L).label(0L).button(0L).build();
        assertNotEquals(a, b);
    }

    @Test
    void testGiiKeyEvent_toString() {
        GiiKeyEvent e = GiiKeyEvent.newBuilder()
                .eventType(5).deviceOrigin(0L).modifiers(0L)
                .symbol(0x61L).label(0L).button(0L).build();
        assertNotNull(e.toString());
    }

    @Test
    void testGiiKeyEvent_fromBuilder() {
        GiiKeyEvent orig = GiiKeyEvent.newBuilder()
                .eventType(7).deviceOrigin(2L).modifiers(4L)
                .symbol(0x44L).label(0L).button(1L).build();
        GiiKeyEvent copy = GiiKeyEvent.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    // -----------------------------------------------------------------------
    // GiiPointerMoveEvent direct read
    // -----------------------------------------------------------------------

    @Test
    void testGiiPointerMoveEvent_directRead() throws IOException {
        GiiPointerMoveEvent orig = GiiPointerMoveEvent.newBuilder()
                .eventType(8).deviceOrigin(1L).x(100).y(200).z(0).wheel(0).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        orig.write(baos, true);
        byte[] bytes = baos.toByteArray();
        InputStream in = new ByteArrayInputStream(bytes, 2, bytes.length - 2);
        GiiPointerMoveEvent copy = GiiPointerMoveEvent.read(in, true);
        assertEquals(100, copy.x());
        assertEquals(200, copy.y());
    }

    @Test
    void testGiiPointerMoveEvent_equals() {
        GiiPointerMoveEvent a = GiiPointerMoveEvent.newBuilder()
                .eventType(8).deviceOrigin(1L).x(50).y(75).z(0).wheel(0).build();
        GiiPointerMoveEvent b = GiiPointerMoveEvent.newBuilder()
                .eventType(8).deviceOrigin(1L).x(50).y(75).z(0).wheel(0).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testGiiPointerMoveEvent_toString() {
        GiiPointerMoveEvent e = GiiPointerMoveEvent.newBuilder()
                .eventType(9).deviceOrigin(0L).x(10).y(20).z(5).wheel(1).build();
        assertNotNull(e.toString());
    }

    @Test
    void testGiiPointerMoveEvent_fromBuilder() {
        GiiPointerMoveEvent orig = GiiPointerMoveEvent.newBuilder()
                .eventType(8).deviceOrigin(3L).x(1).y(2).z(3).wheel(4).build();
        GiiPointerMoveEvent copy = GiiPointerMoveEvent.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    // -----------------------------------------------------------------------
    // GiiPointerButtonEvent direct read
    // -----------------------------------------------------------------------

    @Test
    void testGiiPointerButtonEvent_directRead() throws IOException {
        GiiPointerButtonEvent orig = GiiPointerButtonEvent.newBuilder()
                .eventType(10).deviceOrigin(2L).buttonNumber(3L).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        orig.write(baos, true);
        byte[] bytes = baos.toByteArray();
        InputStream in = new ByteArrayInputStream(bytes, 2, bytes.length - 2);
        GiiPointerButtonEvent copy = GiiPointerButtonEvent.read(in, true);
        assertEquals(3L, copy.buttonNumber());
    }

    @Test
    void testGiiPointerButtonEvent_equals() {
        GiiPointerButtonEvent a = GiiPointerButtonEvent.newBuilder()
                .eventType(10).deviceOrigin(1L).buttonNumber(2L).build();
        GiiPointerButtonEvent b = GiiPointerButtonEvent.newBuilder()
                .eventType(10).deviceOrigin(1L).buttonNumber(2L).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testGiiPointerButtonEvent_toString() {
        GiiPointerButtonEvent e = GiiPointerButtonEvent.newBuilder()
                .eventType(11).deviceOrigin(1L).buttonNumber(1L).build();
        assertNotNull(e.toString());
    }

    @Test
    void testGiiPointerButtonEvent_fromBuilder() {
        GiiPointerButtonEvent orig = GiiPointerButtonEvent.newBuilder()
                .eventType(10).deviceOrigin(4L).buttonNumber(5L).build();
        GiiPointerButtonEvent copy = GiiPointerButtonEvent.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }

    // -----------------------------------------------------------------------
    // GiiValuatorEvent direct read
    // -----------------------------------------------------------------------

    @Test
    void testGiiValuatorEvent_directRead() throws IOException {
        GiiValuatorEvent orig = GiiValuatorEvent.newBuilder()
                .eventType(13).deviceOrigin(1L)
                .first(0L).values(List.of(256)).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        orig.write(baos, true);
        byte[] bytes = baos.toByteArray();
        InputStream in = new ByteArrayInputStream(bytes, 2, bytes.length - 2);
        GiiValuatorEvent copy = GiiValuatorEvent.read(in, true);
        assertEquals(0L, copy.first());
        assertEquals(1, copy.values().size());
    }

    @Test
    void testGiiValuatorEvent_equals() {
        GiiValuatorEvent a = GiiValuatorEvent.newBuilder()
                .eventType(12).deviceOrigin(1L)
                .first(0L).values(List.of(100)).build();
        GiiValuatorEvent b = GiiValuatorEvent.newBuilder()
                .eventType(12).deviceOrigin(1L)
                .first(0L).values(List.of(100)).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testGiiValuatorEvent_notEquals() {
        GiiValuatorEvent a = GiiValuatorEvent.newBuilder()
                .eventType(12).deviceOrigin(1L).first(0L).values(List.of(1)).build();
        GiiValuatorEvent b = GiiValuatorEvent.newBuilder()
                .eventType(12).deviceOrigin(1L).first(0L).values(List.of(2)).build();
        assertNotEquals(a, b);
    }

    @Test
    void testGiiValuatorEvent_toString() {
        GiiValuatorEvent e = GiiValuatorEvent.newBuilder()
                .eventType(13).deviceOrigin(0L).first(0L).values(List.of(512)).build();
        assertNotNull(e.toString());
    }

    @Test
    void testGiiValuatorEvent_fromBuilder() {
        GiiValuatorEvent orig = GiiValuatorEvent.newBuilder()
                .eventType(12).deviceOrigin(2L).first(1L).values(List.of(300, 400)).build();
        GiiValuatorEvent copy = GiiValuatorEvent.newBuilder().from(orig).build();
        assertEquals(orig, copy);
    }
}
