package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleCursor;

import java.awt.image.BufferedImage;

/**
 * Renders {@link RfbRectangleCursor} rectangles (encoding type -239) onto a
 * {@link BufferedImage}.
 *
 * <p>Pixels whose bitmask bit is 1 are decoded from the pixel data and written
 * as fully opaque; pixels whose bitmask bit is 0 are written as fully transparent.
 * The cursor hotspot is at ({@link RfbRectangleCursor#x()},
 * {@link RfbRectangleCursor#y()}) relative to the cursor image origin.
 */
public final class RfbRectangleCursorRender implements RfbRectangleRender<RfbRectangleCursor> {

    private final PixelFormat pixelFormat;

    public RfbRectangleCursorRender(PixelFormat pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    @Override
    public void render(RfbRectangleCursor rectangle, BufferedImage image) {
        int w = rectangle.width();
        int h = rectangle.height();
        if (w <= 0 || h <= 0) return;

        byte[] pixels = rectangle.pixels();
        byte[] bitmask = rectangle.bitmask();
        int bpp = PixelDecoder.bytesPerPixel(pixelFormat);
        int maskRowBytes = (w + 7) / 8;

        int[] argb = new int[w * h];
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                int maskByte = bitmask[dy * maskRowBytes + dx / 8] & 0xFF;
                boolean visible = (maskByte & (0x80 >> (dx % 8))) != 0;
                argb[dy * w + dx] = visible
                        ? PixelDecoder.decodePixel(pixels, (dy * w + dx) * bpp, pixelFormat)
                        : 0x00000000;
            }
        }
        image.setRGB(0, 0, w, h, argb, 0, w);
    }
}
