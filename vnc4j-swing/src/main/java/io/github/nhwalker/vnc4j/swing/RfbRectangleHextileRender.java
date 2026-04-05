package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.HextileSubrect;
import io.github.nhwalker.vnc4j.protocol.HextileTile;
import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleHextile;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Renders {@link RfbRectangleHextile} rectangles (encoding type 5) onto a
 * {@link BufferedImage}.
 *
 * <p>Background and foreground colours persist across tiles within a single
 * rectangle per the Hextile specification.
 */
public final class RfbRectangleHextileRender implements RfbRectangleRender<RfbRectangleHextile> {

    private final PixelFormat pixelFormat;

    public RfbRectangleHextileRender(PixelFormat pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    @Override
    public void render(RfbRectangleHextile rectangle, BufferedImage image) {
        List<HextileTile> tiles = rectangle.tiles();
        int tileIdx = 0;

        int bg = 0xFF000000;
        int fg = 0xFF000000;

        int cols = (rectangle.width() + 15) / 16;
        int rows = (rectangle.height() + 15) / 16;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (tileIdx >= tiles.size()) break;
                HextileTile tile = tiles.get(tileIdx++);

                int tileX = rectangle.x() + col * 16;
                int tileY = rectangle.y() + row * 16;
                int tileW = Math.min(16, rectangle.x() + rectangle.width() - tileX);
                int tileH = Math.min(16, rectangle.y() + rectangle.height() - tileY);

                int subenc = tile.subencoding();

                if ((subenc & HextileTile.SUBENC_RAW) != 0) {
                    PixelDecoder.drawRawPixels(image, tileX, tileY, tileW, tileH,
                            tile.rawPixels(), 0, pixelFormat);
                    continue;
                }

                if ((subenc & HextileTile.SUBENC_BACKGROUND_SPECIFIED) != 0) {
                    bg = PixelDecoder.decodePixel(tile.background(), 0, pixelFormat);
                }
                PixelDecoder.fillRect(image, tileX, tileY, tileW, tileH, bg);

                if ((subenc & HextileTile.SUBENC_FOREGROUND_SPECIFIED) != 0) {
                    fg = PixelDecoder.decodePixel(tile.foreground(), 0, pixelFormat);
                }

                if ((subenc & HextileTile.SUBENC_ANY_SUBRECTS) != 0) {
                    boolean coloured = (subenc & HextileTile.SUBENC_SUBRECTS_COLOURED) != 0;
                    for (HextileSubrect sub : tile.subrects()) {
                        int argb = coloured
                                ? PixelDecoder.decodePixel(sub.pixel(), 0, pixelFormat)
                                : fg;
                        PixelDecoder.fillRect(image,
                                tileX + sub.x(), tileY + sub.y(),
                                sub.width(), sub.height(),
                                argb);
                    }
                }
            }
        }
    }
}
