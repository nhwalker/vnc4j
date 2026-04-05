package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleTightPngFill;

import java.awt.image.BufferedImage;

/**
 * Renders {@link RfbRectangleTightPngFill} rectangles (TightPng encoding type -260,
 * compression 0x8) onto a {@link BufferedImage} by filling the region with a solid
 * TPIXEL colour.
 */
public final class RfbRectangleTightPngFillRender implements RfbRectangleRender<RfbRectangleTightPngFill> {

    private final PixelFormat pixelFormat;

    public RfbRectangleTightPngFillRender(PixelFormat pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    @Override
    public void render(RfbRectangleTightPngFill rectangle, BufferedImage image) {
        int argb = PixelDecoder.decodeTPixel(rectangle.fillColor(), 0, pixelFormat);
        PixelDecoder.fillRect(image,
                rectangle.x(), rectangle.y(),
                rectangle.width(), rectangle.height(),
                argb);
    }
}
