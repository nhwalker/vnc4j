package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleRaw;

import java.awt.image.BufferedImage;

/**
 * Renders an {@link RfbRectangleRaw} (encoding type 0) onto a {@link BufferedImage}.
 * The pixel data is decoded using the supplied {@link PixelFormat}.
 */
public final class RfbRectangleRawRender implements RfbRectangleRender {

    private final RfbRectangleRaw rectangle;
    private final PixelFormat pixelFormat;

    public RfbRectangleRawRender(RfbRectangleRaw rectangle, PixelFormat pixelFormat) {
        this.rectangle = rectangle;
        this.pixelFormat = pixelFormat;
    }

    @Override
    public void render(BufferedImage image) {
        PixelDecoder.drawRawPixels(image,
                rectangle.x(), rectangle.y(),
                rectangle.width(), rectangle.height(),
                rectangle.pixels(), 0, pixelFormat);
    }
}
