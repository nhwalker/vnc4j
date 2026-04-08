package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.messages.RfbRectangleExtendedDesktopSize;

import java.awt.image.BufferedImage;

/**
 * Renderer for {@link RfbRectangleExtendedDesktopSize} (encoding type -308).
 *
 * <p>Carries layout metadata but no pixel data. {@link #render} is a no-op —
 * callers are responsible for handling any framebuffer resize indicated by
 * {@link RfbRectangleExtendedDesktopSize#width()} and
 * {@link RfbRectangleExtendedDesktopSize#height()}.
 */
public final class RfbRectangleExtendedDesktopSizeRender
        implements RfbRectangleRender<RfbRectangleExtendedDesktopSize> {

    public RfbRectangleExtendedDesktopSizeRender() {}

    @Override
    public void render(RfbRectangleExtendedDesktopSize rectangle, BufferedImage image) {
        // No pixel data to apply; framebuffer resize is handled by the caller.
    }
}
