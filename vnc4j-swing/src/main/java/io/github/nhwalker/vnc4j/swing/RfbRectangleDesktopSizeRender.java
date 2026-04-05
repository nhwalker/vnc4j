package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.RfbRectangleDesktopSize;

import java.awt.image.BufferedImage;

/**
 * Renderer for {@link RfbRectangleDesktopSize} (encoding type -223).
 *
 * <p>This rectangle type carries no pixel data; it signals that the remote
 * framebuffer has been resized. {@link #render} is a no-op — callers are
 * responsible for creating a new {@link BufferedImage} of the updated dimensions.
 */
public final class RfbRectangleDesktopSizeRender implements RfbRectangleRender<RfbRectangleDesktopSize> {

    public RfbRectangleDesktopSizeRender() {}

    @Override
    public void render(RfbRectangleDesktopSize rectangle, BufferedImage image) {
        // No pixel data to apply; framebuffer resize is handled by the caller.
    }
}
