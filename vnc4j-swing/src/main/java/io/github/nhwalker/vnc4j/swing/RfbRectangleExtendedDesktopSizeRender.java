package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.RfbRectangleExtendedDesktopSize;

import java.awt.image.BufferedImage;

/**
 * Renderer for {@link RfbRectangleExtendedDesktopSize} (encoding type -308).
 *
 * <p>This rectangle type carries layout metadata (screen list) but no pixel data.
 * The new framebuffer dimensions are carried in
 * {@link RfbRectangleExtendedDesktopSize#width()} and
 * {@link RfbRectangleExtendedDesktopSize#height()}. The {@link #render(BufferedImage)}
 * method is a no-op — callers are responsible for creating a new
 * {@link BufferedImage} of the updated dimensions if required.
 */
public final class RfbRectangleExtendedDesktopSizeRender implements RfbRectangleRender {

    private final RfbRectangleExtendedDesktopSize rectangle;

    public RfbRectangleExtendedDesktopSizeRender(RfbRectangleExtendedDesktopSize rectangle) {
        this.rectangle = rectangle;
    }

    @Override
    public void render(BufferedImage image) {
        // No pixel data to apply; framebuffer resize is handled by the caller.
    }
}
