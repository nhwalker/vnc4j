package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.messages.*;

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
 * <p>Covers: Bell, EndOfContinuousUpdates, ClientFence, ServerFence, ClientCutText, ServerCutText.
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

}
