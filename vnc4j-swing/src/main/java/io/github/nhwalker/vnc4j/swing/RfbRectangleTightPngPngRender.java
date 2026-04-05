package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.RfbRectangleTightPngPng;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Renders {@link RfbRectangleTightPngPng} rectangles (TightPng encoding type -260,
 * compression 0xA) onto a {@link BufferedImage} by decoding the embedded PNG data.
 */
public final class RfbRectangleTightPngPngRender implements RfbRectangleRender<RfbRectangleTightPngPng> {

    public RfbRectangleTightPngPngRender() {}

    @Override
    public void render(RfbRectangleTightPngPng rectangle, BufferedImage image) {
        byte[] png = rectangle.pngData();
        if (png == null || png.length == 0) return;

        BufferedImage decoded;
        try {
            decoded = ImageIO.read(new ByteArrayInputStream(png));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to decode PNG data in TightPngPng rectangle", e);
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
