package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleTightPngFill;

import java.awt.image.BufferedImage;

/**
 * Renders an {@link RfbRectangleTightPngFill} (TightPng encoding type -260, compression 0x8)
 * onto a {@link BufferedImage} by filling the region with a solid TPIXEL colour.
 */
public final class RfbRectangleTightPngFillRender implements RfbRectangleRender {

    private final RfbRectangleTightPngFill rectangle;
    private final PixelFormat pixelFormat;

    public RfbRectangleTightPngFillRender(RfbRectangleTightPngFill rectangle, PixelFormat pixelFormat) {
        this.rectangle = rectangle;
        this.pixelFormat = pixelFormat;
    }

    @Override
    public void render(BufferedImage image) {
        int argb = PixelDecoder.decodeTPixel(rectangle.fillColor(), 0, pixelFormat);
        PixelDecoder.fillRect(image,
                rectangle.x(), rectangle.y(),
                rectangle.width(), rectangle.height(),
                argb);
    }
}
