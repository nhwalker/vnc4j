package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.RfbRectangleTightJpeg;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Renders an {@link RfbRectangleTightJpeg} (Tight encoding type 7, compression 0x9)
 * onto a {@link BufferedImage} by decoding the embedded JPEG data.
 */
public final class RfbRectangleTightJpegRender implements RfbRectangleRender {

    private final RfbRectangleTightJpeg rectangle;

    public RfbRectangleTightJpegRender(RfbRectangleTightJpeg rectangle) {
        this.rectangle = rectangle;
    }

    @Override
    public void render(BufferedImage image) {
        byte[] jpeg = rectangle.jpegData();
        if (jpeg == null || jpeg.length == 0) return;

        BufferedImage decoded;
        try {
            decoded = ImageIO.read(new ByteArrayInputStream(jpeg));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to decode JPEG data in TightJpeg rectangle", e);
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
