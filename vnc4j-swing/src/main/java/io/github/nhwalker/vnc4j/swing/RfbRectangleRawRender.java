package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleRaw;

import java.awt.image.BufferedImage;

/**
 * Renders {@link RfbRectangleRaw} rectangles (encoding type 0) onto a
 * {@link BufferedImage}. The pixel data is decoded using the {@link PixelFormat}
 * supplied at construction time.
 */
public final class RfbRectangleRawRender implements RfbRectangleRender<RfbRectangleRaw> {

    private final PixelFormat pixelFormat;

    public RfbRectangleRawRender(PixelFormat pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    @Override
    public void render(RfbRectangleRaw rectangle, BufferedImage image) {
        PixelDecoder.drawRawPixels(image,
                rectangle.x(), rectangle.y(),
                rectangle.width(), rectangle.height(),
                rectangle.pixels(), 0, pixelFormat);
    }
}
