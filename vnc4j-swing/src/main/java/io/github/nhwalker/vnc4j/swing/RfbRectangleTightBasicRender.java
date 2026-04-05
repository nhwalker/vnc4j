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
        int[] argb = new int[w];
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                argb[dx] = PixelDecoder.decodeTPixel(data, (dy * w + dx) * tpxSize, pixelFormat);
            }
            image.setRGB(rect.x(), rect.y() + dy, w, 1, argb, 0, w);
        }
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

        int[] argb = new int[w];
        if (palSize == 2) {
            // 1-bit packed, MSB first, each row padded to a byte boundary
            int byteIdx = 0;
            for (int dy = 0; dy < h; dy++) {
                int bits = 0;
                int bitsLeft = 0;
                for (int dx = 0; dx < w; dx++) {
                    if (bitsLeft == 0) {
                        bits = data[byteIdx++] & 0xFF;
                        bitsLeft = 8;
                    }
                    argb[dx] = palette[(bits >> 7) & 1];
                    bits <<= 1;
                    bitsLeft--;
                }
                image.setRGB(rect.x(), rect.y() + dy, w, 1, argb, 0, w);
            }
        } else {
            // 1-byte index per pixel
            for (int dy = 0; dy < h; dy++) {
                for (int dx = 0; dx < w; dx++) {
                    int idx = data[dy * w + dx] & 0xFF;
                    argb[dx] = (idx < palSize) ? palette[idx] : 0xFF000000;
                }
                image.setRGB(rect.x(), rect.y() + dy, w, 1, argb, 0, w);
            }
        }
    }

    private void renderGradient(BufferedImage image, byte[] data, int w, int h,
            int tpxSize, RfbRectangleTightBasic rect) {
        int[] prev = new int[w];
        int[] argb = new int[w];

        for (int dy = 0; dy < h; dy++) {
            int leftR = 0, leftG = 0, leftB = 0;
            for (int dx = 0; dx < w; dx++) {
                int upR = 0, upG = 0, upB = 0;
                int upLeftR = 0, upLeftG = 0, upLeftB = 0;
                if (dy > 0) {
                    int up = prev[dx];
                    upR = (up >> 16) & 0xFF; upG = (up >> 8) & 0xFF; upB = up & 0xFF;
                    if (dx > 0) {
                        int upLeft = prev[dx - 1];
                        upLeftR = (upLeft >> 16) & 0xFF;
                        upLeftG = (upLeft >> 8) & 0xFF;
                        upLeftB = upLeft & 0xFF;
                    }
                }
                int base = (dy * w + dx) * tpxSize;
                int r = (clamp(leftR + upR - upLeftR) + (data[base]     & 0xFF)) & 0xFF;
                int g = (clamp(leftG + upG - upLeftG) + (data[base + 1] & 0xFF)) & 0xFF;
                int b = (clamp(leftB + upB - upLeftB) + (data[base + 2] & 0xFF)) & 0xFF;

                argb[dx] = 0xFF000000 | (r << 16) | (g << 8) | b;
                leftR = r; leftG = g; leftB = b;
            }
            System.arraycopy(argb, 0, prev, 0, w);
            image.setRGB(rect.x(), rect.y() + dy, w, 1, argb, 0, w);
        }
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
