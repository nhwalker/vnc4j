package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Utility methods for decoding VNC pixel data into ARGB integers for use with
 * {@link BufferedImage}.
 */
final class PixelDecoder {

    private PixelDecoder() {}

    /** Number of bytes per full VNC pixel. */
    static int bytesPerPixel(PixelFormat fmt) {
        return fmt.bitsPerPixel() / 8;
    }

    /**
     * Size of a TPIXEL used in Tight encoding.
     *
     * <p>Per the spec, a TPIXEL is 3 bytes (rather than 4) only when
     * {@code true-colour-flag} is set, {@code bits-per-pixel} is 32,
     * {@code depth} is 24, and every RGB channel is exactly 8 bits wide
     * ({@code redMax == greenMax == blueMax == 255}). Otherwise a TPIXEL
     * is the same size as a full PIXEL ({@link #bytesPerPixel}).
     */
    static int tpixelSize(PixelFormat fmt) {
        if (fmt.trueColour() && fmt.bitsPerPixel() == 32 && fmt.depth() == 24
                && fmt.redMax() == 255 && fmt.greenMax() == 255 && fmt.blueMax() == 255) {
            return 3;
        }
        return bytesPerPixel(fmt);
    }

    /**
     * Size of a CPIXEL used in ZRLE encoding: 3 bytes when bitsPerPixel is 32,
     * depth ≤ 24, and the format uses true colour; otherwise the same as
     * {@link #bytesPerPixel}.
     */
    static int cpixelSize(PixelFormat fmt) {
        return (fmt.bitsPerPixel() == 32 && fmt.depth() <= 24 && fmt.trueColour())
                ? 3 : bytesPerPixel(fmt);
    }

    /**
     * Decodes one full VNC pixel (bytesPerPixel bytes) starting at {@code offset}
     * in {@code data} and returns it as a 32-bit ARGB value with full opacity.
     */
    static int decodePixel(byte[] data, int offset, PixelFormat fmt) {
        int bpp = bytesPerPixel(fmt);
        int raw = 0;
        if (fmt.bigEndian()) {
            for (int i = 0; i < bpp; i++) {
                raw = (raw << 8) | (data[offset + i] & 0xFF);
            }
        } else {
            for (int i = bpp - 1; i >= 0; i--) {
                raw = (raw << 8) | (data[offset + i] & 0xFF);
            }
        }
        return rawToArgb(raw, fmt);
    }

    /**
     * Decodes one TPIXEL (Tight encoding) starting at {@code offset} and returns
     * a 32-bit ARGB value. For 32-bit true-colour formats the TPIXEL is 3 bytes
     * (the padding zero byte is omitted); otherwise it is treated as a full pixel.
     */
    static int decodeTPixel(byte[] data, int offset, PixelFormat fmt) {
        if (fmt.trueColour() && fmt.bitsPerPixel() == 32 && fmt.depth() == 24
                && fmt.redMax() == 255 && fmt.greenMax() == 255 && fmt.blueMax() == 255) {
            byte[] expanded = new byte[4];
            if (fmt.bigEndian()) {
                // Full pixel is [pad, b1, b2, b3]; TPIXEL is [b1, b2, b3]
                System.arraycopy(data, offset, expanded, 1, 3);
            } else {
                // Full pixel is [b0, b1, b2, pad]; TPIXEL is [b0, b1, b2]
                System.arraycopy(data, offset, expanded, 0, 3);
            }
            return decodePixel(expanded, 0, fmt);
        }
        return decodePixel(data, offset, fmt);
    }

    /**
     * Decodes one CPIXEL (ZRLE encoding) starting at {@code offset}. CPIXEL is
     * identical to TPIXEL when bitsPerPixel is 32 and depth ≤ 24; otherwise it
     * is a full pixel.
     */
    static int decodeCPixel(byte[] data, int offset, PixelFormat fmt) {
        if (fmt.bitsPerPixel() == 32 && fmt.depth() <= 24 && fmt.trueColour()) {
            return decodeTPixel(data, offset, fmt);
        }
        return decodePixel(data, offset, fmt);
    }

    /** Fills a rectangular region of an image with a solid ARGB colour. */
    static void fillRect(BufferedImage image, int x, int y, int w, int h, int argb) {
        if (w <= 0 || h <= 0) return;
        Graphics2D g = image.createGraphics();
        try {
            g.setColor(new Color(argb, true));
            g.fillRect(x, y, w, h);
        } finally {
            g.dispose();
        }
    }

    /**
     * Draws {@code w × h} raw VNC pixels (each {@link #bytesPerPixel} bytes long)
     * from {@code pixels} starting at byte {@code dataOffset} into the image at
     * position ({@code x}, {@code y}).
     */
    static void drawRawPixels(BufferedImage image, int x, int y, int w, int h,
            byte[] pixels, int dataOffset, PixelFormat fmt) {
        int bpp = bytesPerPixel(fmt);
        int[] argb = new int[w * h];
        for (int i = 0; i < argb.length; i++) {
            argb[i] = decodePixel(pixels, dataOffset + i * bpp, fmt);
        }
        image.setRGB(x, y, w, h, argb, 0, w);
    }

    /**
     * Decompresses {@code compressed} using the supplied persistent {@link Inflater} and
     * returns all decompressed bytes. The inflater is <em>not</em> reset or closed, so
     * its stream context is preserved for subsequent calls (required by VNC encodings that
     * maintain a continuous zlib stream across rectangles).
     *
     * <p>The caller must use {@link Inflater#setInput} before the first call if the
     * inflater has no pending input yet; this method calls {@link Inflater#setInput}
     * internally with {@code compressed}.
     */
    static byte[] inflate(Inflater inflater, byte[] compressed) {
        inflater.setInput(compressed);
        ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(compressed.length * 4, 256));
        byte[] buf = new byte[4096];
        try {
            int n;
            while (!inflater.needsInput() && !inflater.finished()) {
                n = inflater.inflate(buf);
                out.write(buf, 0, n);
            }
        } catch (DataFormatException e) {
            throw new IllegalStateException("Zlib decompression failed", e);
        }
        return out.toByteArray();
    }

    // -------------------------------------------------------------------------

    private static int rawToArgb(int raw, PixelFormat fmt) {
        if (!fmt.trueColour()) {
            return 0xFF000000;
        }
        int r = (raw >> fmt.redShift()) & fmt.redMax();
        int g = (raw >> fmt.greenShift()) & fmt.greenMax();
        int b = (raw >> fmt.blueShift()) & fmt.blueMax();
        return 0xFF000000
                | (scale(r, fmt.redMax()) << 16)
                | (scale(g, fmt.greenMax()) << 8)
                | scale(b, fmt.blueMax());
    }

    private static int scale(int value, int max) {
        return max > 0 ? (value * 255 + max / 2) / max : 0;
    }
}
