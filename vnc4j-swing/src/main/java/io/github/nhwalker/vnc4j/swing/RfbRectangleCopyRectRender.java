package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.RfbRectangleCopyRect;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Renders an {@link RfbRectangleCopyRect} (encoding type 1) onto a {@link BufferedImage}.
 *
 * <p>Copies a region of the same image from the source position
 * ({@link RfbRectangleCopyRect#srcX()}, {@link RfbRectangleCopyRect#srcY()}) to
 * the destination ({@link RfbRectangleCopyRect#x()}, {@link RfbRectangleCopyRect#y()}).
 */
public final class RfbRectangleCopyRectRender implements RfbRectangleRender {

    private final RfbRectangleCopyRect rectangle;

    public RfbRectangleCopyRectRender(RfbRectangleCopyRect rectangle) {
        this.rectangle = rectangle;
    }

    @Override
    public void render(BufferedImage image) {
        int w = rectangle.width();
        int h = rectangle.height();
        if (w <= 0 || h <= 0) return;

        // Capture the source region first to handle overlapping copies correctly
        int[] srcPixels = image.getRGB(rectangle.srcX(), rectangle.srcY(), w, h, null, 0, w);
        image.setRGB(rectangle.x(), rectangle.y(), w, h, srcPixels, 0, w);
    }
}
