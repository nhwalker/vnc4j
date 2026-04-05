package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleZlib;

import java.awt.image.BufferedImage;
import java.util.zip.Inflater;

/**
 * Renders {@link RfbRectangleZlib} rectangles (encoding type 6) onto a
 * {@link BufferedImage}.
 *
 * <p>VNC Zlib encoding uses a <em>single persistent zlib stream</em> across all
 * rectangles for the lifetime of the connection. This renderer maintains that
 * stream as a field so that the decompressor's history is correctly preserved
 * between calls to {@link #render}.
 */
public final class RfbRectangleZlibRender implements RfbRectangleRender<RfbRectangleZlib> {

    private final PixelFormat pixelFormat;
    private final Inflater inflater = new Inflater();

    public RfbRectangleZlibRender(PixelFormat pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    @Override
    public void render(RfbRectangleZlib rectangle, BufferedImage image) {
        byte[] compressed = rectangle.zlibData();
        if (compressed == null || compressed.length == 0) return;

        byte[] pixels = PixelDecoder.inflate(inflater, compressed);

        PixelDecoder.drawRawPixels(image,
                rectangle.x(), rectangle.y(),
                rectangle.width(), rectangle.height(),
                pixels, 0, pixelFormat);
    }
}
