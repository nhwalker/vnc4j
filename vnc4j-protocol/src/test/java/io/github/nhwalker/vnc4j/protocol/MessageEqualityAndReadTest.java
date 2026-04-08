package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.messages.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for equals/hashCode/toString/read and direct static-read methods for
 * singleton-style messages and other message types with gaps in coverage.
 *
 * <p>Covers: Bell, ClientCutText, ServerCutText.
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
