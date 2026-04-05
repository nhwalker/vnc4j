package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.RfbRectangleXCursor;

import java.awt.image.BufferedImage;

/**
 * Renders an {@link RfbRectangleXCursor} (encoding type -240) onto a
 * {@link BufferedImage}.
 *
 * <p>The cursor image uses two colours: a primary (foreground) colour and a
 * secondary (background) colour. The {@code bitmap} field determines which
 * pixels receive the primary colour and the {@code bitmask} field determines
 * which pixels are visible. Pixels that are not visible are written as fully
 * transparent.
 *
 * <p>The cursor hotspot is at ({@link RfbRectangleXCursor#x()},
 * {@link RfbRectangleXCursor#y()}) relative to the cursor image origin.
 */
public final class RfbRectangleXCursorRender implements RfbRectangleRender {

    private final RfbRectangleXCursor rectangle;

    public RfbRectangleXCursorRender(RfbRectangleXCursor rectangle) {
        this.rectangle = rectangle;
    }

    @Override
    public void render(BufferedImage image) {
        int w = rectangle.width();
        int h = rectangle.height();
        if (w <= 0 || h <= 0) return;

        int primaryArgb = 0xFF000000
                | (clamp(rectangle.primaryR()) << 16)
                | (clamp(rectangle.primaryG()) << 8)
                | clamp(rectangle.primaryB());
        int secondaryArgb = 0xFF000000
                | (clamp(rectangle.secondaryR()) << 16)
                | (clamp(rectangle.secondaryG()) << 8)
                | clamp(rectangle.secondaryB());

        byte[] bitmap  = rectangle.bitmap();   // foreground mask
        byte[] bitmask = rectangle.bitmask();  // valid-pixel mask
        int rowBytes = (w + 7) / 8;

        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                int byteIdx = dy * rowBytes + dx / 8;
                int bitMask = 0x80 >> (dx % 8);

                boolean visible  = (bitmask[byteIdx] & bitMask) != 0;
                boolean isFg     = (bitmap[byteIdx]  & bitMask) != 0;

                int argb = visible ? (isFg ? primaryArgb : secondaryArgb) : 0x00000000;
                image.setRGB(dx, dy, argb);
            }
        }
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
