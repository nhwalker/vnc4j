package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.CoRreSubrect;
import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleCoRre;

import java.awt.image.BufferedImage;

/**
 * Renders an {@link RfbRectangleCoRre} (encoding type 4) onto a {@link BufferedImage}.
 * Identical in structure to RRE but subrectangle coordinates are 8-bit.
 */
public final class RfbRectangleCoRreRender implements RfbRectangleRender {

    private final RfbRectangleCoRre rectangle;
    private final PixelFormat pixelFormat;

    public RfbRectangleCoRreRender(RfbRectangleCoRre rectangle, PixelFormat pixelFormat) {
        this.rectangle = rectangle;
        this.pixelFormat = pixelFormat;
    }

    @Override
    public void render(BufferedImage image) {
        int bgArgb = PixelDecoder.decodePixel(rectangle.background(), 0, pixelFormat);
        PixelDecoder.fillRect(image,
                rectangle.x(), rectangle.y(),
                rectangle.width(), rectangle.height(),
                bgArgb);

        for (CoRreSubrect sub : rectangle.subrects()) {
            int argb = PixelDecoder.decodePixel(sub.pixel(), 0, pixelFormat);
            PixelDecoder.fillRect(image,
                    rectangle.x() + sub.x(), rectangle.y() + sub.y(),
                    sub.width(), sub.height(),
                    argb);
        }
    }
}
