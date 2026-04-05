package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleCursor;

import java.awt.image.BufferedImage;

/**
 * Renders an {@link RfbRectangleCursor} (encoding type -239) onto a {@link BufferedImage}.
 *
 * <p>The cursor hotspot is at ({@code rectangle.x()}, {@code rectangle.y()}) relative
 * to the cursor image origin. This renderer draws the cursor shape at position (0, 0)
 * of the supplied image, masking pixels using the 1-bit bitmask.
 *
 * <p>Pixels whose bitmask bit is 0 are made transparent (written as fully transparent
 * black), while pixels whose bitmask bit is 1 are decoded from the pixel data and
 * written as fully opaque.
 */
public final class RfbRectangleCursorRender implements RfbRectangleRender {

    private final RfbRectangleCursor rectangle;
    private final PixelFormat pixelFormat;

    public RfbRectangleCursorRender(RfbRectangleCursor rectangle, PixelFormat pixelFormat) {
        this.rectangle = rectangle;
        this.pixelFormat = pixelFormat;
    }

    @Override
    public void render(BufferedImage image) {
        int w = rectangle.width();
        int h = rectangle.height();
        if (w <= 0 || h <= 0) return;

        byte[] pixels = rectangle.pixels();
        byte[] bitmask = rectangle.bitmask();
        int bpp = PixelDecoder.bytesPerPixel(pixelFormat);
        int maskRowBytes = (w + 7) / 8;

        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                int maskByte = bitmask[dy * maskRowBytes + dx / 8] & 0xFF;
                boolean visible = (maskByte & (0x80 >> (dx % 8))) != 0;

                int argb;
                if (visible) {
                    argb = PixelDecoder.decodePixel(pixels, (dy * w + dx) * bpp, pixelFormat);
                } else {
                    argb = 0x00000000;
                }
                image.setRGB(dx, dy, argb);
            }
        }
    }
}
