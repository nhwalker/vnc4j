package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleTightBasic;

import java.awt.image.BufferedImage;
import java.util.zip.Inflater;

/**
 * Renders {@link RfbRectangleTightBasic} rectangles (Tight encoding type 7,
 * compression 0x0–0x7) onto a {@link BufferedImage}.
 *
 * <p>Tight encoding maintains <em>four independent persistent zlib streams</em>
 * (indexed 0–3). This renderer holds all four as fields so their contexts survive
 * between rectangles. Each rectangle may reset one or more streams via bits 0–3 of
 * {@link RfbRectangleTightBasic#streamResets()}; a reset replaces the corresponding
 * inflater with a fresh instance.
 *
 * <p>Three filter modes are supported:
 * <ul>
 *   <li>{@link RfbRectangleTightBasic#FILTER_COPY} – raw TPIXEL rows</li>
 *   <li>{@link RfbRectangleTightBasic#FILTER_PALETTE} – palette-indexed data</li>
 *   <li>{@link RfbRectangleTightBasic#FILTER_GRADIENT} – gradient-predicted TPIXEL data</li>
 * </ul>
 */
public final class RfbRectangleTightBasicRender
        implements RfbRectangleRender<RfbRectangleTightBasic> {

    private final PixelFormat pixelFormat;
    private final Inflater[] streams = new Inflater[]{
            new Inflater(), new Inflater(), new Inflater(), new Inflater()
    };

    public RfbRectangleTightBasicRender(PixelFormat pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    @Override
    public void render(RfbRectangleTightBasic rectangle, BufferedImage image) {
        resetStreams(rectangle.streamResets());

        int w = rectangle.width();
        int h = rectangle.height();
        int tpxSize = PixelDecoder.tpixelSize(pixelFormat);
        Inflater stream = streams[rectangle.streamNumber()];

        byte[] decompressed = PixelDecoder.inflate(stream, rectangle.compressedData());

        switch (rectangle.filterType()) {
            case RfbRectangleTightBasic.FILTER_COPY    -> renderCopy(image, decompressed, w, h, tpxSize, rectangle);
            case RfbRectangleTightBasic.FILTER_PALETTE -> renderPalette(image, decompressed, w, h, rectangle);
            case RfbRectangleTightBasic.FILTER_GRADIENT -> renderGradient(image, decompressed, w, h, tpxSize, rectangle);
            default -> renderCopy(image, decompressed, w, h, tpxSize, rectangle);
        }
    }

    private void resetStreams(int resetBits) {
        for (int i = 0; i < 4; i++) {
            if ((resetBits & (1 << i)) != 0) {
                streams[i].end();
                streams[i] = new Inflater();
            }
        }
    }

    private void renderCopy(BufferedImage image, byte[] data, int w, int h,
            int tpxSize, RfbRectangleTightBasic rect) {
        int[] argb = new int[w * h];
        for (int i = 0; i < argb.length; i++) {
            argb[i] = PixelDecoder.decodeTPixel(data, i * tpxSize, pixelFormat);
        }
        image.setRGB(rect.x(), rect.y(), w, h, argb, 0, w);
    }

    private void renderPalette(BufferedImage image, byte[] data, int w, int h,
            RfbRectangleTightBasic rect) {
        int tpxSize = PixelDecoder.tpixelSize(pixelFormat);
        int palSize = rect.paletteSize();
        byte[] palBytes = rect.palette();

        int[] palette = new int[palSize];
        for (int i = 0; i < palSize; i++) {
            palette[i] = PixelDecoder.decodeTPixel(palBytes, i * tpxSize, pixelFormat);
        }

        int[] argb = new int[w * h];
        if (palSize == 2) {
            // 1-bit packed, MSB first, each row padded to a byte boundary
            int byteIdx = 0;
            int pixIdx = 0;
            for (int dy = 0; dy < h; dy++) {
                int bits = 0;
                int bitsLeft = 0;
                for (int dx = 0; dx < w; dx++) {
                    if (bitsLeft == 0) {
                        bits = data[byteIdx++] & 0xFF;
                        bitsLeft = 8;
                    }
                    argb[pixIdx++] = palette[(bits >> 7) & 1];
                    bits <<= 1;
                    bitsLeft--;
                }
            }
        } else {
            // 1-byte index per pixel
            for (int i = 0; i < w * h; i++) {
                int idx = data[i] & 0xFF;
                argb[i] = (idx < palSize) ? palette[idx] : 0xFF000000;
            }
        }
        image.setRGB(rect.x(), rect.y(), w, h, argb, 0, w);
    }

    private void renderGradient(BufferedImage image, byte[] data, int w, int h,
            int tpxSize, RfbRectangleTightBasic rect) {
        // The gradient algorithm operates on three separate 8-bit colour channels
        // (R, G, B) packed as the first, second and third byte of each TPIXEL.
        // Per the spec a TPIXEL is 3 bytes only for 32-bit true-colour; for any
        // other format tpxSize != 3, meaning the data is NOT laid out as three
        // independent 8-bit channels.  Gradient decoding is undefined for those
        // formats (the spec only permits GradientFilter when bits-per-pixel is 16
        // or 32, but 16-bit TPIXEL is a packed 2-byte value, not three channels).
        // Fall back to the raw CopyFilter path so we still render something.
        if (tpxSize != 3) {
            renderCopy(image, data, w, h, tpxSize, rect);
            return;
        }

        // Accumulate all decoded pixels into a single array; read the previous row
        // from the same array to avoid a separate prev[] buffer and arraycopy.
        int[] allPixels = new int[w * h];

        for (int dy = 0; dy < h; dy++) {
            int leftR = 0, leftG = 0, leftB = 0;
            for (int dx = 0; dx < w; dx++) {
                int upR = 0, upG = 0, upB = 0;
                int upLeftR = 0, upLeftG = 0, upLeftB = 0;
                if (dy > 0) {
                    int up = allPixels[(dy - 1) * w + dx];
                    upR = (up >> 16) & 0xFF; upG = (up >> 8) & 0xFF; upB = up & 0xFF;
                    if (dx > 0) {
                        int upLeft = allPixels[(dy - 1) * w + (dx - 1)];
                        upLeftR = (upLeft >> 16) & 0xFF;
                        upLeftG = (upLeft >> 8) & 0xFF;
                        upLeftB = upLeft & 0xFF;
                    }
                }
                int base = (dy * w + dx) * 3;
                int r = (clamp(leftR + upR - upLeftR) + (data[base]     & 0xFF)) & 0xFF;
                int g = (clamp(leftG + upG - upLeftG) + (data[base + 1] & 0xFF)) & 0xFF;
                int b = (clamp(leftB + upB - upLeftB) + (data[base + 2] & 0xFF)) & 0xFF;

                allPixels[dy * w + dx] = 0xFF000000 | (r << 16) | (g << 8) | b;
                leftR = r; leftG = g; leftB = b;
            }
        }
        image.setRGB(rect.x(), rect.y(), w, h, allPixels, 0, w);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
