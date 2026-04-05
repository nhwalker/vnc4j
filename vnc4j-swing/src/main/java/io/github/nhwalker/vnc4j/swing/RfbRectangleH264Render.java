package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.RfbRectangleH264;

import java.awt.image.BufferedImage;

/**
 * Placeholder renderer for {@link RfbRectangleH264} (encoding type 50).
 *
 * <p>H.264 decoding requires platform-specific native libraries and is not
 * implemented. Calling {@link #render} throws {@link UnsupportedOperationException}.
 */
public final class RfbRectangleH264Render implements RfbRectangleRender<RfbRectangleH264> {

    public RfbRectangleH264Render() {}

    @Override
    public void render(RfbRectangleH264 rectangle, BufferedImage image) {
        throw new UnsupportedOperationException(
                "H.264 decoding is not supported by this renderer");
    }
}
