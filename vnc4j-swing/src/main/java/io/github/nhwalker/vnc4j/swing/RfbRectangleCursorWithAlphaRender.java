package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.RfbRectangleCursorWithAlpha;

import java.awt.image.BufferedImage;

/**
 * Renders {@link RfbRectangleCursorWithAlpha} rectangles (encoding type -314)
 * onto a {@link BufferedImage}.
 *
 * <p>When the nested {@link RfbRectangleCursorWithAlpha#encoding()} is raw (0),
 * the {@link RfbRectangleCursorWithAlpha#data()} field contains
 * {@code width × height} 32-bit RGBA pixels. Other nested encodings are not
 * currently supported and result in a no-op.
 */
public final class RfbRectangleCursorWithAlphaRender
        implements RfbRectangleRender<RfbRectangleCursorWithAlpha> {

    private static final int RAW_ENCODING = 0;

    public RfbRectangleCursorWithAlphaRender() {}

    @Override
    public void render(RfbRectangleCursorWithAlpha rectangle, BufferedImage image) {
        int w = rectangle.width();
        int h = rectangle.height();
        if (w <= 0 || h <= 0) return;
        if (rectangle.encoding() != RAW_ENCODING) return;

        byte[] data = rectangle.data();
        if (data == null || data.length < w * h * 4) return;

        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                int base = (dy * w + dx) * 4;
                int r = data[base]     & 0xFF;
                int g = data[base + 1] & 0xFF;
                int b = data[base + 2] & 0xFF;
                int a = data[base + 3] & 0xFF;
                image.setRGB(dx, dy, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
    }
}
