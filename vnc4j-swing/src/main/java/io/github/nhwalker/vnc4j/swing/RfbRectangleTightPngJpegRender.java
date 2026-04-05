package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.RfbRectangleTightPngJpeg;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Renders {@link RfbRectangleTightPngJpeg} rectangles (TightPng encoding type -260,
 * compression 0x9) onto a {@link BufferedImage} by decoding the embedded JPEG data.
 */
public final class RfbRectangleTightPngJpegRender implements RfbRectangleRender<RfbRectangleTightPngJpeg> {

    public RfbRectangleTightPngJpegRender() {}

    @Override
    public void render(RfbRectangleTightPngJpeg rectangle, BufferedImage image) {
        byte[] jpeg = rectangle.jpegData();
        if (jpeg == null || jpeg.length == 0) return;

        BufferedImage decoded;
        try {
            decoded = ImageIO.read(new ByteArrayInputStream(jpeg));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to decode JPEG data in TightPngJpeg rectangle", e);
        }
        if (decoded == null) return;

        Graphics2D g = image.createGraphics();
        try {
            g.drawImage(decoded, rectangle.x(), rectangle.y(), null);
        } finally {
            g.dispose();
        }
    }
}
