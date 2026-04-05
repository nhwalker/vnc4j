package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleTightBasic;

import java.awt.image.BufferedImage;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Renders an {@link RfbRectangleTightBasic} (Tight encoding type 7, compression 0x0–0x7)
 * onto a {@link BufferedImage}.
 *
 * <p>Three filter types are supported:
 * <ul>
 *   <li>{@link RfbRectangleTightBasic#FILTER_COPY} – raw TPIXEL data after zlib decompression</li>
 *   <li>{@link RfbRectangleTightBasic#FILTER_PALETTE} – palette-indexed data after decompression</li>
 *   <li>{@link RfbRectangleTightBasic#FILTER_GRADIENT} – gradient-filtered TPIXEL data</li>
 * </ul>
 *
 * <p><b>Note:</b> Tight encoding maintains persistent per-stream zlib contexts across
 * rectangles. This renderer decompresses the stored bytes as a self-contained stream,
 * which is correct for the first rectangle but may fail for subsequent rectangles that
 * continue a persistent stream.
 */
public final class RfbRectangleTightBasicRender implements RfbRectangleRender {

    private final RfbRectangleTightBasic rectangle;
    private final PixelFormat pixelFormat;

    public RfbRectangleTightBasicRender(RfbRectangleTightBasic rectangle, PixelFormat pixelFormat) {
        this.rectangle = rectangle;
        this.pixelFormat = pixelFormat;
    }

    @Override
    public void render(BufferedImage image) {
        int w = rectangle.width();
        int h = rectangle.height();
        int tpxSize = PixelDecoder.tpixelSize(pixelFormat);

        byte[] decompressed = decompress(rectangle.compressedData(), w * h * tpxSize);

        switch (rectangle.filterType()) {
            case RfbRectangleTightBasic.FILTER_COPY -> renderCopy(image, decompressed, w, h, tpxSize);
            case RfbRectangleTightBasic.FILTER_PALETTE -> renderPalette(image, decompressed, w, h);
            case RfbRectangleTightBasic.FILTER_GRADIENT -> renderGradient(image, decompressed, w, h, tpxSize);
            default -> renderCopy(image, decompressed, w, h, tpxSize);
        }
    }

    private void renderCopy(BufferedImage image, byte[] data, int w, int h, int tpxSize) {
        int[] argb = new int[w];
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                argb[dx] = PixelDecoder.decodeTPixel(data, (dy * w + dx) * tpxSize, pixelFormat);
            }
            image.setRGB(rectangle.x(), rectangle.y() + dy, w, 1, argb, 0, w);
        }
    }

    private void renderPalette(BufferedImage image, byte[] data, int w, int h) {
        int tpxSize = PixelDecoder.tpixelSize(pixelFormat);
        int palSize = rectangle.paletteSize();
        byte[] palBytes = rectangle.palette();

        int[] palette = new int[palSize];
        for (int i = 0; i < palSize; i++) {
            palette[i] = PixelDecoder.decodeTPixel(palBytes, i * tpxSize, pixelFormat);
        }

        int[] argb = new int[w];
        if (palSize == 2) {
            // 1-bit packed per row
            int byteIdx = 0;
            for (int dy = 0; dy < h; dy++) {
                int bits = 0;
                int bitsLeft = 0;
                for (int dx = 0; dx < w; dx++) {
                    if (bitsLeft == 0) {
                        bits = data[byteIdx++] & 0xFF;
                        bitsLeft = 8;
                    }
                    int idx = (bits >> 7) & 1;
                    bits <<= 1;
                    bitsLeft--;
                    argb[dx] = palette[idx];
                }
                image.setRGB(rectangle.x(), rectangle.y() + dy, w, 1, argb, 0, w);
            }
        } else {
            // 1-byte index per pixel
            for (int dy = 0; dy < h; dy++) {
                for (int dx = 0; dx < w; dx++) {
                    int idx = data[dy * w + dx] & 0xFF;
                    argb[dx] = (idx < palSize) ? palette[idx] : 0xFF000000;
                }
                image.setRGB(rectangle.x(), rectangle.y() + dy, w, 1, argb, 0, w);
            }
        }
    }

    private void renderGradient(BufferedImage image, byte[] data, int w, int h, int tpxSize) {
        // Gradient filter: each pixel is stored as a delta from the prediction.
        // Prediction is the sum of the left pixel, the upper pixel, and
        // the upper-left pixel, clamped to [0, 255] per channel (24-bit RGB assumed).
        int[] prev = new int[w];
        int[] argb = new int[w];
        int[] cur = new int[w * 3];

        for (int dy = 0; dy < h; dy++) {
            // Decode row bytes (3 bytes per pixel for gradient filter)
            for (int dx = 0; dx < w; dx++) {
                int base = (dy * w + dx) * tpxSize;
                cur[dx * 3]     = data[base]     & 0xFF;
                cur[dx * 3 + 1] = data[base + 1] & 0xFF;
                cur[dx * 3 + 2] = data[base + 2] & 0xFF;
            }

            int leftR = 0, leftG = 0, leftB = 0;
            for (int dx = 0; dx < w; dx++) {
                int upR = 0, upG = 0, upB = 0;
                int upLeftR = 0, upLeftG = 0, upLeftB = 0;
                if (dy > 0) {
                    int up = prev[dx];
                    upR = (up >> 16) & 0xFF;
                    upG = (up >> 8) & 0xFF;
                    upB = up & 0xFF;
                    if (dx > 0) {
                        int upLeft = prev[dx - 1];
                        upLeftR = (upLeft >> 16) & 0xFF;
                        upLeftG = (upLeft >> 8) & 0xFF;
                        upLeftB = upLeft & 0xFF;
                    }
                }
                int predR = clamp(leftR + upR - upLeftR);
                int predG = clamp(leftG + upG - upLeftG);
                int predB = clamp(leftB + upB - upLeftB);

                int r = (predR + cur[dx * 3])     & 0xFF;
                int g = (predG + cur[dx * 3 + 1]) & 0xFF;
                int b = (predB + cur[dx * 3 + 2]) & 0xFF;

                argb[dx] = 0xFF000000 | (r << 16) | (g << 8) | b;
                leftR = r; leftG = g; leftB = b;
            }
            System.arraycopy(argb, 0, prev, 0, w);
            image.setRGB(rectangle.x(), rectangle.y() + dy, w, 1, argb, 0, w);
        }
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private static byte[] decompress(byte[] compressed, int expectedSize) {
        if (compressed == null || compressed.length == 0) {
            return new byte[expectedSize];
        }
        byte[] out = new byte[expectedSize];
        Inflater inflater = new Inflater();
        try {
            inflater.setInput(compressed);
            inflater.inflate(out);
        } catch (DataFormatException e) {
            throw new IllegalStateException("Failed to decompress Tight basic rectangle data", e);
        } finally {
            inflater.end();
        }
        return out;
    }
}
