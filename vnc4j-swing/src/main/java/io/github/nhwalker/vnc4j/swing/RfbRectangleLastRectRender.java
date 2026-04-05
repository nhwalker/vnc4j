package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.RfbRectangleLastRect;

import java.awt.image.BufferedImage;

/**
 * Renderer for {@link RfbRectangleLastRect} (encoding type -224).
 *
 * <p>This rectangle type carries no pixel data; it is a sentinel that signals
 * the end of a framebuffer update. {@link #render(BufferedImage)} is a no-op.
 */
public final class RfbRectangleLastRectRender implements RfbRectangleRender {

    private final RfbRectangleLastRect rectangle;

    public RfbRectangleLastRectRender(RfbRectangleLastRect rectangle) {
        this.rectangle = rectangle;
    }

    @Override
    public void render(BufferedImage image) {
        // No pixel data to apply.
    }
}
