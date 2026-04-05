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

    @Test
    void testQemuAudioServerEnd_roundTrip() throws IOException {
        // write: 255(type), 1(sub), U16(0=op); read() called after type+sub+op consumed = skip 4
        QemuAudioServerEnd original = QemuAudioServerEnd.newBuilder().build();
        QemuAudioServerEnd result = QemuAudioServerEnd.read(
                afterBytes(original::write, 4));
        assertNotNull(result);
    }

    @Test
    void testQemuAudioServerBegin_roundTrip() throws IOException {
        // write: 255(type), 1(sub), U16(1=op); read() called after type+sub+op consumed = skip 4
        QemuAudioServerBegin original = QemuAudioServerBegin.newBuilder().build();
        QemuAudioServerBegin result = QemuAudioServerBegin.read(
                afterBytes(original::write, 4));
        assertNotNull(result);
    }

    @Test
    void testQemuAudioServerData_roundTrip() throws IOException {
        // write: 255(type), 1(sub), U16(2=op), U32(len), data;
        // read() called after type+sub+op (4 bytes) consumed = skip 4
        byte[] audioData = new byte[]{0x10, 0x20, 0x30, 0x40};
        QemuAudioServerData original = QemuAudioServerData.newBuilder().data(audioData).build();
        QemuAudioServerData result = QemuAudioServerData.read(
                afterBytes(original::write, 4));
        assertArrayEquals(original.data(), result.data());
    }

    // -----------------------------------------------------------------------
    // GII messages (skip 1 type byte)
    // -----------------------------------------------------------------------

    @Test
    void testGiiClientVersion_bigEndian_roundTrip() throws IOException {
        GiiClientVersion original = GiiClientVersion.newBuilder()
                .bigEndian(true).version(1).build();
        GiiClientVersion result = GiiClientVersion.read(afterTypeByte(original::write));
        assertEquals(original.bigEndian(), result.bigEndian());
        assertEquals(original.version(), result.version());
    }

    @Test
    void testGiiClientVersion_littleEndian_roundTrip() throws IOException {
        GiiClientVersion original = GiiClientVersion.newBuilder()
                .bigEndian(false).version(1).build();
        GiiClientVersion result = GiiClientVersion.read(afterTypeByte(original::write));
        assertEquals(original.bigEndian(), result.bigEndian());
        assertEquals(original.version(), result.version());
    }

    @Test
    void testGiiServerVersion_roundTrip() throws IOException {
        GiiServerVersion original = GiiServerVersion.newBuilder()
                .bigEndian(true).maximumVersion(5).minimumVersion(1).build();
        GiiServerVersion result = GiiServerVersion.read(afterTypeByte(original::write));
        assertEquals(original.bigEndian(), result.bigEndian());
        assertEquals(original.maximumVersion(), result.maximumVersion());
        assertEquals(original.minimumVersion(), result.minimumVersion());
    }

    @Test
    void testGiiDeviceCreationResponse_roundTrip() throws IOException {
        GiiDeviceCreationResponse original = GiiDeviceCreationResponse.newBuilder()
                .bigEndian(true).deviceOrigin(42L).build();
        GiiDeviceCreationResponse result = GiiDeviceCreationResponse.read(
                afterTypeByte(original::write));
        assertEquals(original.bigEndian(), result.bigEndian());
        assertEquals(original.deviceOrigin(), result.deviceOrigin());
    }

    @Test
    void testGiiDeviceDestruction_roundTrip() throws IOException {
        GiiDeviceDestruction original = GiiDeviceDestruction.newBuilder()
                .bigEndian(true).deviceOrigin(7L).build();
        GiiDeviceDestruction result = GiiDeviceDestruction.read(afterTypeByte(original::write));
        assertEquals(original.deviceOrigin(), result.deviceOrigin());
    }

    @Test
    void testGiiDeviceCreation_roundTrip() throws IOException {
        GiiValuator val = GiiValuator.newBuilder()
                .index(0L).longName("X Axis").shortName("X")
                .rangeMin(-500).rangeCenter(0).rangeMax(500)
                .siUnit(0L).siAdd(0).siMul(1).siDiv(1).siShift(0)
                .build();
        GiiDeviceCreation original = GiiDeviceCreation.newBuilder()
                .bigEndian(true)
                .deviceName("TestPad")
                .vendorId(0x046DL).productId(0xC219L)
                .canGenerate(0L).numRegisters(0L)
                .valuators(List.of(val))
                .numButtons(4L)
                .build();
        GiiDeviceCreation result = GiiDeviceCreation.read(afterTypeByte(original::write));
        assertEquals(original.bigEndian(), result.bigEndian());
        assertEquals("TestPad", result.deviceName());
        assertEquals(original.vendorId(), result.vendorId());
        assertEquals(original.productId(), result.productId());
        assertEquals(original.numButtons(), result.numButtons());
        assertEquals(1, result.valuators().size());
        assertEquals("X Axis", result.valuators().get(0).longName());
        assertEquals(-500, result.valuators().get(0).rangeMin());
        assertEquals(500, result.valuators().get(0).rangeMax());
    }

    // -----------------------------------------------------------------------
    // QEMU client messages (skip 1 type byte)
    // -----------------------------------------------------------------------

    @Test
    void testQemuExtendedKeyEvent_roundTrip() throws IOException {
        // write: 255(type), 0(sub), U16(downFlag), ...; read() called after type+sub (2 bytes)
        QemuExtendedKeyEvent original = QemuExtendedKeyEvent.newBuilder()
                .downFlag(1).keysym(0x61).keycode(30).build();
        QemuExtendedKeyEvent result = QemuExtendedKeyEvent.read(
                afterBytes(original::write, 2));
        assertEquals(original.downFlag(), result.downFlag());
        assertEquals(original.keysym(), result.keysym());
        assertEquals(original.keycode(), result.keycode());
    }

    @Test
    void testQemuAudioClientSetFormat_roundTrip() throws IOException {
        // write: 255(type), 1(sub), U16(2=op), fields; read() called after type+sub+op (4 bytes)
        QemuAudioClientSetFormat original = QemuAudioClientSetFormat.newBuilder()
                .sampleFormat(1).nchannels(2).frequency(44100L).build();
        QemuAudioClientSetFormat result = QemuAudioClientSetFormat.read(
                afterBytes(original::write, 4));
        assertEquals(original.sampleFormat(), result.sampleFormat());
        assertEquals(original.nchannels(), result.nchannels());
        assertEquals(original.frequency(), result.frequency());
    }
}
