package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.RfbRectangleCopyRect;

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

        // Capture source region first to handle overlapping copies correctly.
        int[] srcPixels = image.getRGB(rectangle.srcX(), rectangle.srcY(), w, h, null, 0, w);
        image.setRGB(rectangle.x(), rectangle.y(), w, h, srcPixels, 0, w);
    }
}
