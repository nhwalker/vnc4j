package io.github.nhwalker.vnc4j.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Round-trip tests that verify write → read produces identical objects.
 *
 * <p><b>Important note on stream position:</b> The {@code write()} methods include
 * the message-type byte as first byte, but the {@code read()} methods are called by
 * the dispatcher AFTER it has already consumed the type byte. Therefore round-trip
 * tests must skip 1 byte (the type byte) before calling {@code read()} on
 * client/server messages.
 *
 * <p>Handshaking messages (ProtocolVersion, SecurityTypeSelection, ClientInit) are
 * read from the beginning of the stream with no type byte to skip.
 */
class RoundTripTest {

    /** Serialize a message and return a stream positioned AFTER the type byte. */
    private InputStream afterTypeByte(Writable w) throws IOException {
        return afterBytes(w, 1);
    }

    /** Serialize a message and return a stream positioned after skipping {@code skip} bytes. */
    private InputStream afterBytes(Writable w, int skip) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        w.write(baos);
        byte[] bytes = baos.toByteArray();
        return new ByteArrayInputStream(bytes, skip, bytes.length - skip);
    }

    @FunctionalInterface
    interface Writable {
        void write(java.io.OutputStream out) throws IOException;
    }

    // -----------------------------------------------------------------------
    // Handshaking (full stream, no type byte to skip)
    // -----------------------------------------------------------------------

    @Test
    void testProtocolVersion_roundTrip() throws IOException {
        ProtocolVersion original = ProtocolVersion.newBuilder().major(3).minor(8).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        original.write(baos);
        ProtocolVersion result = ProtocolVersion.read(new ByteArrayInputStream(baos.toByteArray()));
        assertEquals(original.major(), result.major());
        assertEquals(original.minor(), result.minor());
    }

    @Test
    void testSecurityTypeSelection_roundTrip() throws IOException {
        SecurityTypeSelection original = SecurityTypeSelection.newBuilder()
                .securityType(2).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        original.write(baos);
        SecurityTypeSelection result = SecurityTypeSelection.read(
                new ByteArrayInputStream(baos.toByteArray()));
        assertEquals(original.securityType(), result.securityType());
    }

    @Test
    void testClientInit_roundTrip() throws IOException {
        ClientInit original = ClientInit.newBuilder().shared(true).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        original.write(baos);
        ClientInit result = ClientInit.read(new ByteArrayInputStream(baos.toByteArray()));
        assertEquals(original.shared(), result.shared());
    }

    // -----------------------------------------------------------------------
    // Client messages (skip 1 type byte)
    // -----------------------------------------------------------------------

    @Test
    void testSetPixelFormat_roundTrip() throws IOException {
        PixelFormat pf = PixelFormat.newBuilder()
                .bitsPerPixel(32).depth(24).bigEndian(true).trueColour(true)
                .redMax(255).greenMax(255).blueMax(255)
                .redShift(16).greenShift(8).blueShift(0)
                .build();
        SetPixelFormat original = SetPixelFormat.newBuilder().pixelFormat(pf).build();
        SetPixelFormat result = SetPixelFormat.read(afterTypeByte(original::write));
        assertEquals(original.pixelFormat().bitsPerPixel(), result.pixelFormat().bitsPerPixel());
        assertEquals(original.pixelFormat().depth(), result.pixelFormat().depth());
        assertEquals(original.pixelFormat().redMax(), result.pixelFormat().redMax());
        assertEquals(original.pixelFormat().bigEndian(), result.pixelFormat().bigEndian());
    }

    @Test
    void testSetEncodings_roundTrip() throws IOException {
        SetEncodings original = SetEncodings.newBuilder()
                .encodings(List.of(0, 1, 2, -239))
                .build();
        SetEncodings result = SetEncodings.read(afterTypeByte(original::write));
        assertEquals(original.encodings(), result.encodings());
    }

    @Test
    void testFramebufferUpdateRequest_roundTrip() throws IOException {
        FramebufferUpdateRequest original = FramebufferUpdateRequest.newBuilder()
                .incremental(true).x(10).y(20).width(640).height(480).build();
        FramebufferUpdateRequest result = FramebufferUpdateRequest.read(
                afterTypeByte(original::write));
        assertEquals(original.incremental(), result.incremental());
        assertEquals(original.x(), result.x());
        assertEquals(original.y(), result.y());
        assertEquals(original.width(), result.width());
        assertEquals(original.height(), result.height());
    }

    @Test
    void testKeyEvent_roundTrip() throws IOException {
        KeyEvent original = KeyEvent.newBuilder().down(true).key(0x61).build();
        KeyEvent result = KeyEvent.read(afterTypeByte(original::write));
        assertEquals(original.down(), result.down());
        assertEquals(original.key(), result.key());
    }

    @Test
    void testPointerEvent_roundTrip() throws IOException {
        PointerEvent original = PointerEvent.newBuilder()
                .buttonMask(3).x(100).y(200).build();
        PointerEvent result = PointerEvent.read(afterTypeByte(original::write));
        assertEquals(original.buttonMask(), result.buttonMask());
        assertEquals(original.x(), result.x());
        assertEquals(original.y(), result.y());
    }

    @Test
    void testClientCutText_roundTrip() throws IOException {
        byte[] text = "hello".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        ClientCutText original = ClientCutText.newBuilder().text(text).build();
        ClientCutText result = ClientCutText.read(afterTypeByte(original::write));
        assertArrayEquals(original.text(), result.text());
    }

    // -----------------------------------------------------------------------
    // Extended client messages (skip 1 type byte)
    // -----------------------------------------------------------------------

    @Test
    void testEnableContinuousUpdates_roundTrip() throws IOException {
        EnableContinuousUpdates original = EnableContinuousUpdates.newBuilder()
                .enable(true).x(0).y(0).width(800).height(600).build();
        EnableContinuousUpdates result = EnableContinuousUpdates.read(
                afterTypeByte(original::write));
        assertEquals(original.enable(), result.enable());
        assertEquals(original.width(), result.width());
        assertEquals(original.height(), result.height());
    }

    @Test
    void testClientFence_roundTrip() throws IOException {
        ClientFence original = ClientFence.newBuilder()
                .flags(0x7).payload(new byte[]{0x11, 0x22}).build();
        ClientFence result = ClientFence.read(afterTypeByte(original::write));
        assertEquals(original.flags(), result.flags());
        assertArrayEquals(original.payload(), result.payload());
    }

    @Test
    void testXvpClientMessage_roundTrip() throws IOException {
        XvpClientMessage original = XvpClientMessage.newBuilder()
                .xvpVersion(1).xvpMessageCode(3).build();
        XvpClientMessage result = XvpClientMessage.read(afterTypeByte(original::write));
        assertEquals(original.xvpVersion(), result.xvpVersion());
        assertEquals(original.xvpMessageCode(), result.xvpMessageCode());
    }

    @Test
    void testSetDesktopSize_roundTrip() throws IOException {
        Screen screen = Screen.newBuilder()
                .id(1).x(0).y(0).width(1280).height(720).flags(0).build();
        SetDesktopSize original = SetDesktopSize.newBuilder()
                .width(1280).height(720)
                .screens(List.of(screen))
                .build();
        SetDesktopSize result = SetDesktopSize.read(afterTypeByte(original::write));
        assertEquals(original.width(), result.width());
        assertEquals(original.height(), result.height());
        assertEquals(1, result.screens().size());
        assertEquals(screen.id(), result.screens().get(0).id());
        assertEquals(screen.width(), result.screens().get(0).width());
    }

    // -----------------------------------------------------------------------
    // Server messages (skip 1 type byte)
    // -----------------------------------------------------------------------

    @Test
    void testBell_roundTrip() throws IOException {
        Bell original = Bell.newBuilder().build();
        Bell result = Bell.read(afterTypeByte(original::write));
        assertNotNull(result);
    }

    @Test
    void testServerCutText_roundTrip() throws IOException {
        byte[] text = "clipboard content".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        ServerCutText original = ServerCutText.newBuilder().text(text).build();
        ServerCutText result = ServerCutText.read(afterTypeByte(original::write));
        assertArrayEquals(original.text(), result.text());
    }

    @Test
    void testSetColourMapEntries_roundTrip() throws IOException {
        ColourMapEntry entry = ColourMapEntry.newBuilder()
                .red(0xFFFF).green(0x8000).blue(0x0000).build();
        SetColourMapEntries original = SetColourMapEntries.newBuilder()
                .firstColour(5).colours(List.of(entry)).build();
        SetColourMapEntries result = SetColourMapEntries.read(
                afterTypeByte(original::write));
        assertEquals(original.firstColour(), result.firstColour());
        assertEquals(1, result.colours().size());
        assertEquals(entry.red(), result.colours().get(0).red());
        assertEquals(entry.green(), result.colours().get(0).green());
        assertEquals(entry.blue(), result.colours().get(0).blue());
    }

    // -----------------------------------------------------------------------
    // Extended server messages (skip 1 type byte)
    // -----------------------------------------------------------------------

    @Test
    void testEndOfContinuousUpdates_roundTrip() throws IOException {
        EndOfContinuousUpdates original = EndOfContinuousUpdates.newBuilder().build();
        EndOfContinuousUpdates result = EndOfContinuousUpdates.read(
                afterTypeByte(original::write));
        assertNotNull(result);
    }

    @Test
    void testServerFence_roundTrip() throws IOException {
        ServerFence original = ServerFence.newBuilder()
                .flags(0x3).payload(new byte[]{(byte) 0xAA, (byte) 0xBB}).build();
        ServerFence result = ServerFence.read(afterTypeByte(original::write));
        assertEquals(original.flags(), result.flags());
        assertArrayEquals(original.payload(), result.payload());
    }

    @Test
    void testXvpServerMessage_roundTrip() throws IOException {
        XvpServerMessage original = XvpServerMessage.newBuilder()
                .xvpVersion(1).xvpMessageCode(2).build();
        XvpServerMessage result = XvpServerMessage.read(afterTypeByte(original::write));
        assertEquals(original.xvpVersion(), result.xvpVersion());
        assertEquals(original.xvpMessageCode(), result.xvpMessageCode());
    }

}
