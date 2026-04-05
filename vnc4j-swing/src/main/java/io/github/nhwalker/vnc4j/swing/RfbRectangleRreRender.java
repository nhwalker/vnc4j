package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleRre;
import io.github.nhwalker.vnc4j.protocol.RreSubrect;

import java.awt.image.BufferedImage;

/**
 * Renders {@link RfbRectangleRre} rectangles (encoding type 2) onto a
 * {@link BufferedImage}. Fills the background colour then overlays each subrectangle.
 */
public final class RfbRectangleRreRender implements RfbRectangleRender<RfbRectangleRre> {

    private final PixelFormat pixelFormat;

    public RfbRectangleRreRender(PixelFormat pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    @Override
    public void render(RfbRectangleRre rectangle, BufferedImage image) {
        int bgArgb = PixelDecoder.decodePixel(rectangle.background(), 0, pixelFormat);
        PixelDecoder.fillRect(image,
                rectangle.x(), rectangle.y(),
                rectangle.width(), rectangle.height(),
                bgArgb);

        for (RreSubrect sub : rectangle.subrects()) {
            int argb = PixelDecoder.decodePixel(sub.pixel(), 0, pixelFormat);
            PixelDecoder.fillRect(image,
                    rectangle.x() + sub.x(), rectangle.y() + sub.y(),
                    sub.width(), sub.height(),
                    argb);
        }
    }
}
