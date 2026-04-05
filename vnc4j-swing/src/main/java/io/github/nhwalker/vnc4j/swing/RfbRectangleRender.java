package io.github.nhwalker.vnc4j.swing;

import java.awt.image.BufferedImage;

/**
 * Applies an {@link io.github.nhwalker.vnc4j.protocol.RfbRectangle} to a {@link BufferedImage}.
 */
public interface RfbRectangleRender {
    /**
     * Applies the rectangle's pixel update to the given image.
     *
     * @param image the framebuffer image to update in-place
     */
    void render(BufferedImage image);
}
