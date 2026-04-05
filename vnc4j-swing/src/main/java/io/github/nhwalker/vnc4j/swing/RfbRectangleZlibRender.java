package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleZlib;

import java.awt.image.BufferedImage;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Renders an {@link RfbRectangleZlib} (encoding type 6) onto a {@link BufferedImage}.
 *
 * <p>The zlib-compressed payload is decompressed and then treated as raw pixel data.
 */
public final class RfbRectangleZlibRender implements RfbRectangleRender {

    private final RfbRectangleZlib rectangle;
    private final PixelFormat pixelFormat;

    public RfbRectangleZlibRender(RfbRectangleZlib rectangle, PixelFormat pixelFormat) {
        this.rectangle = rectangle;
        this.pixelFormat = pixelFormat;
    }

    @Override
    public void render(BufferedImage image) {
        byte[] compressed = rectangle.zlibData();
        if (compressed == null || compressed.length == 0) return;

        int bpp = PixelDecoder.bytesPerPixel(pixelFormat);
        int pixelCount = rectangle.width() * rectangle.height();
        byte[] pixels = new byte[pixelCount * bpp];

        Inflater inflater = new Inflater();
        try {
            inflater.setInput(compressed);
            inflater.inflate(pixels);
        } catch (DataFormatException e) {
            throw new IllegalStateException("Failed to decompress Zlib rectangle data", e);
        } finally {
            inflater.end();
        }

        PixelDecoder.drawRawPixels(image,
                rectangle.x(), rectangle.y(),
                rectangle.width(), rectangle.height(),
                pixels, 0, pixelFormat);
    }
}
