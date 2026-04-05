package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleTightFill;

import java.awt.image.BufferedImage;

/**
 * Renders an {@link RfbRectangleTightFill} (Tight encoding type 7, compression 0x8)
 * onto a {@link BufferedImage} by filling the region with a solid TPIXEL colour.
 */
public final class RfbRectangleTightFillRender implements RfbRectangleRender {

    private final RfbRectangleTightFill rectangle;
    private final PixelFormat pixelFormat;

    public RfbRectangleTightFillRender(RfbRectangleTightFill rectangle, PixelFormat pixelFormat) {
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
