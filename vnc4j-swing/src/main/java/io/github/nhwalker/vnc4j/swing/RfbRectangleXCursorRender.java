package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.RfbRectangleXCursor;

import java.awt.image.BufferedImage;

/**
 * Renders {@link RfbRectangleXCursor} rectangles (encoding type -240) onto a
 * {@link BufferedImage}.
 *
 * <p>Uses a two-colour scheme: the {@code bitmap} determines which pixels receive
 * the primary (foreground) colour and the {@code bitmask} determines which pixels
 * are visible. Invisible pixels are written as fully transparent.
 */
public final class RfbRectangleXCursorRender implements RfbRectangleRender<RfbRectangleXCursor> {

    public RfbRectangleXCursorRender() {}

    @Override
    public void render(RfbRectangleXCursor rectangle, BufferedImage image) {
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

        byte[] bitmap  = rectangle.bitmap();
        byte[] bitmask = rectangle.bitmask();
        int rowBytes = (w + 7) / 8;

        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                int byteIdx = dy * rowBytes + dx / 8;
                int bitMask = 0x80 >> (dx % 8);

                boolean visible = (bitmask[byteIdx] & bitMask) != 0;
                boolean isFg    = (bitmap[byteIdx]  & bitMask) != 0;

                image.setRGB(dx, dy, visible ? (isFg ? primaryArgb : secondaryArgb) : 0x00000000);
            }
        }
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
