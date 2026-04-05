package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.RfbRectangle;

import java.awt.image.BufferedImage;

/**
 * Applies an {@link RfbRectangle} to a {@link BufferedImage}.
 *
 * <p>Implementations are long-lived objects (one per VNC connection) so that
 * encoding-specific state — such as persistent zlib decompression streams —
 * can be maintained across rectangles.
 *
 * @param <T> the concrete {@link RfbRectangle} subtype this renderer handles
 */
public interface RfbRectangleRender<T extends RfbRectangle> {
    /**
     * Applies the given rectangle's pixel update to the framebuffer image.
     *
     * @param rectangle the rectangle to render
     * @param image     the framebuffer image to update in-place
     */
    void render(T rectangle, BufferedImage image);
}
