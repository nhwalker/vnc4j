package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.RfbRectangleCopyRect;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Renders {@link RfbRectangleCopyRect} rectangles (encoding type 1) onto a
 * {@link BufferedImage}. Copies a region of the same image from
 * ({@link RfbRectangleCopyRect#srcX()}, {@link RfbRectangleCopyRect#srcY()}) to
 * ({@link RfbRectangleCopyRect#x()}, {@link RfbRectangleCopyRect#y()}).
 */
public final class RfbRectangleCopyRectRender implements RfbRectangleRender<RfbRectangleCopyRect> {

    public RfbRectangleCopyRectRender() {}

    @Override
    public void render(RfbRectangleCopyRect rectangle, BufferedImage image) {
        int w = rectangle.width();
        int h = rectangle.height();
        if (w <= 0 || h <= 0) return;

        // copyArea handles overlapping regions correctly and avoids allocating a
        // temporary pixel buffer; it is also eligible for hardware acceleration.
        Graphics2D g = image.createGraphics();
        try {
            g.copyArea(rectangle.srcX(), rectangle.srcY(), w, h,
                    rectangle.x() - rectangle.srcX(), rectangle.y() - rectangle.srcY());
        } finally {
            g.dispose();
        }
    }
}
