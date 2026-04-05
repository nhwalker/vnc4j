package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleTightFill;

import java.awt.image.BufferedImage;

/**
 * Renders {@link RfbRectangleTightFill} rectangles (Tight encoding type 7,
 * compression 0x8) onto a {@link BufferedImage} by filling the region with a
 * solid TPIXEL colour.
 */
public final class RfbRectangleTightFillRender implements RfbRectangleRender<RfbRectangleTightFill> {

    private final PixelFormat pixelFormat;

    public RfbRectangleTightFillRender(PixelFormat pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    @Override
    public void render(RfbRectangleTightFill rectangle, BufferedImage image) {
        int argb = PixelDecoder.decodeTPixel(rectangle.fillColor(), 0, pixelFormat);
        PixelDecoder.fillRect(image,
                rectangle.x(), rectangle.y(),
                rectangle.width(), rectangle.height(),
                argb);
    }
}
