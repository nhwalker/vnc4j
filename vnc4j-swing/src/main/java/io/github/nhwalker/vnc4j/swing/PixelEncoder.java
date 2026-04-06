package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;

import java.awt.image.BufferedImage;

/**
 * Utility methods for encoding ARGB pixel data from a {@link BufferedImage}
 * into the raw VNC wire format described by a {@link PixelFormat}.
 *
 * <p>This is the inverse of {@link PixelDecoder}.
 */
final class PixelEncoder {

    private PixelEncoder() {}

    /**
     * Encodes the rectangular region {@code [x, y, width, height]} of {@code img}
     * into a packed byte array using the supplied {@link PixelFormat}.
     *
     * <p>The result is {@code width × height × bytesPerPixel(fmt)} bytes, in
     * row-major order (top row first, left pixel first within each row).
     */
    static byte[] encodeRegion(BufferedImage img, int x, int y, int w, int h, PixelFormat fmt) {
        int bpp = fmt.bitsPerPixel() / 8;
        byte[] out = new byte[w * h * bpp];
        int offset = 0;
        for (int row = y; row < y + h; row++) {
            for (int col = x; col < x + w; col++) {
                encodePixelInto(img.getRGB(col, row), fmt, bpp, out, offset);
                offset += bpp;
            }
        }
        return out;
    }

    /**
     * Encodes a single ARGB pixel (ignoring alpha) as {@code bytesPerPixel(fmt)} bytes.
     */
    static byte[] encodePixel(int argb, PixelFormat fmt) {
        int bpp = fmt.bitsPerPixel() / 8;
        byte[] out = new byte[bpp];
        encodePixelInto(argb, fmt, bpp, out, 0);
        return out;
    }

    // -------------------------------------------------------------------------

    private static void encodePixelInto(int argb, PixelFormat fmt, int bpp, byte[] out, int offset) {
        if (!fmt.trueColour()) {
            // Non-true-colour is not supported for server-side encoding; write zeros.
            return;
        }
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;

        // Scale each channel from [0,255] to [0, channelMax].
        int rs = (fmt.redMax() == 255) ? r : (r * fmt.redMax() + 127) / 255;
        int gs = (fmt.greenMax() == 255) ? g : (g * fmt.greenMax() + 127) / 255;
        int bs = (fmt.blueMax() == 255) ? b : (b * fmt.blueMax() + 127) / 255;

        int raw = (rs << fmt.redShift()) | (gs << fmt.greenShift()) | (bs << fmt.blueShift());

        if (fmt.bigEndian()) {
            // Most significant byte at lowest index.
            for (int i = 0; i < bpp; i++) {
                out[offset + i] = (byte) ((raw >> ((bpp - 1 - i) * 8)) & 0xFF);
            }
        } else {
            // Least significant byte at lowest index.
            for (int i = 0; i < bpp; i++) {
                out[offset + i] = (byte) (raw & 0xFF);
                raw >>>= 8;
            }
        }
    }
}
