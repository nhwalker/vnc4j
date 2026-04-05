package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.RfbRectangleJpeg;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Renders an {@link RfbRectangleJpeg} (encoding type 21) onto a {@link BufferedImage}
 * by decoding the embedded JPEG data.
 */
public final class RfbRectangleJpegRender implements RfbRectangleRender {

    private final RfbRectangleJpeg rectangle;

    public RfbRectangleJpegRender(RfbRectangleJpeg rectangle) {
        this.rectangle = rectangle;
    }

    @Override
    public void render(BufferedImage image) {
        byte[] jpeg = rectangle.data();
        if (jpeg == null || jpeg.length == 0) return;

        BufferedImage decoded;
        try {
            decoded = ImageIO.read(new ByteArrayInputStream(jpeg));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to decode JPEG data in Jpeg rectangle", e);
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
