package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that {@link PixelEncoder} inverts {@link PixelDecoder} for common formats.
 */
class PixelEncoderTest {

    private static PixelFormat bgr0LittleEndian() {
        return PixelFormat.newBuilder()
                .bitsPerPixel(32).depth(24)
                .bigEndian(false).trueColour(true)
                .redMax(255).redShift(16)
                .greenMax(255).greenShift(8)
                .blueMax(255).blueShift(0)
                .build();
    }

    private static PixelFormat rgb0BigEndian() {
        return PixelFormat.newBuilder()
                .bitsPerPixel(32).depth(24)
                .bigEndian(true).trueColour(true)
                .redMax(255).redShift(16)
                .greenMax(255).greenShift(8)
                .blueMax(255).blueShift(0)
                .build();
    }

    // -------------------------------------------------------------------------
    // encodePixel
    // -------------------------------------------------------------------------

    @Test
    void encodePixel_bgr0LittleEndian_redPixel() {
        // ARGB 0xFFFF0000 = red
        byte[] encoded = PixelEncoder.encodePixel(0xFFFF0000, bgr0LittleEndian());
        assertEquals(4, encoded.length);
        // Little-endian: LSB first → [B=0, G=0, R=255, pad=0]
        assertEquals(0x00, encoded[0] & 0xFF, "blue");
        assertEquals(0x00, encoded[1] & 0xFF, "green");
        assertEquals(0xFF, encoded[2] & 0xFF, "red");
        assertEquals(0x00, encoded[3] & 0xFF, "padding");
    }

    @Test
    void encodePixel_bgr0LittleEndian_greenPixel() {
        byte[] encoded = PixelEncoder.encodePixel(0xFF00FF00, bgr0LittleEndian());
        assertEquals(0x00, encoded[0] & 0xFF, "blue");
        assertEquals(0xFF, encoded[1] & 0xFF, "green");
        assertEquals(0x00, encoded[2] & 0xFF, "red");
    }

    @Test
    void encodePixel_bigEndian_bluePixel() {
        byte[] encoded = PixelEncoder.encodePixel(0xFF0000FF, rgb0BigEndian());
        // Big-endian: MSB first → [pad=0, R=0, G=0, B=255]
        assertEquals(0x00, encoded[0] & 0xFF, "padding");
        assertEquals(0x00, encoded[1] & 0xFF, "red");
        assertEquals(0x00, encoded[2] & 0xFF, "green");
        assertEquals(0xFF, encoded[3] & 0xFF, "blue");
    }

    // -------------------------------------------------------------------------
    // Round-trip: encodePixel → decodePixel
    // -------------------------------------------------------------------------

    @Test
    void roundTrip_littleEndian_knownColor() {
        int original = 0xFFABCDEF; // arbitrary ARGB
        PixelFormat fmt = bgr0LittleEndian();
        byte[] encoded = PixelEncoder.encodePixel(original, fmt);
        int decoded = PixelDecoder.decodePixel(encoded, 0, fmt);
        // Alpha is always set to FF by decodePixel, so compare RGB only
        assertEquals(original & 0x00FFFFFF, decoded & 0x00FFFFFF);
    }

    @Test
    void roundTrip_bigEndian_knownColor() {
        int original = 0xFF123456;
        PixelFormat fmt = rgb0BigEndian();
        byte[] encoded = PixelEncoder.encodePixel(original, fmt);
        int decoded = PixelDecoder.decodePixel(encoded, 0, fmt);
        assertEquals(original & 0x00FFFFFF, decoded & 0x00FFFFFF);
    }

    // -------------------------------------------------------------------------
    // encodeRegion
    // -------------------------------------------------------------------------

    @Test
    void encodeRegion_2x2_littleEndian() {
        // Build a 2×2 image: top-left red, top-right green, bottom-left blue, bottom-right white
        BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, 0xFFFF0000); // red
        img.setRGB(1, 0, 0xFF00FF00); // green
        img.setRGB(0, 1, 0xFF0000FF); // blue
        img.setRGB(1, 1, 0xFFFFFFFF); // white

        PixelFormat fmt = bgr0LittleEndian();
        byte[] encoded = PixelEncoder.encodeRegion(img, 0, 0, 2, 2, fmt);
        assertEquals(16, encoded.length); // 4 pixels × 4 bytes

        // Decode each pixel and check
        int red   = PixelDecoder.decodePixel(encoded, 0,  fmt);
        int green = PixelDecoder.decodePixel(encoded, 4,  fmt);
        int blue  = PixelDecoder.decodePixel(encoded, 8,  fmt);
        int white = PixelDecoder.decodePixel(encoded, 12, fmt);

        assertEquals(0xFF0000, red   & 0xFFFFFF, "red pixel");
        assertEquals(0x00FF00, green & 0xFFFFFF, "green pixel");
        assertEquals(0x0000FF, blue  & 0xFFFFFF, "blue pixel");
        assertEquals(0xFFFFFF, white & 0xFFFFFF, "white pixel");
    }

    @Test
    void encodeRegion_subRegion_onlyEncodesRequestedArea() {
        BufferedImage img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(1, 1, 0xFFFF0000); // red at (1,1)

        PixelFormat fmt = bgr0LittleEndian();
        // Encode only the 1×1 sub-region at (1,1)
        byte[] encoded = PixelEncoder.encodeRegion(img, 1, 1, 1, 1, fmt);
        assertEquals(4, encoded.length);
        int pixel = PixelDecoder.decodePixel(encoded, 0, fmt);
        assertEquals(0xFF0000, pixel & 0xFFFFFF);
    }
}
