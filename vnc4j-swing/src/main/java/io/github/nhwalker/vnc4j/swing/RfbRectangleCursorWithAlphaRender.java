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

        int[] argb = new int[w * h];
        for (int i = 0; i < argb.length; i++) {
            int base = i * 4;
            int rPre = data[base]     & 0xFF;
            int gPre = data[base + 1] & 0xFF;
            int bPre = data[base + 2] & 0xFF;
            int a    = data[base + 3] & 0xFF;
            // The spec stores pre-multiplied RGBA; BufferedImage.setRGB expects
            // straight (non-pre-multiplied) ARGB, so un-premultiply here.
            int r, g, b;
            if (a == 0) {
                r = g = b = 0;
            } else {
                r = Math.min(255, (rPre * 255 + a / 2) / a);
                g = Math.min(255, (gPre * 255 + a / 2) / a);
                b = Math.min(255, (bPre * 255 + a / 2) / a);
            }
            argb[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
        image.setRGB(0, 0, w, h, argb, 0, w);
    }
}
