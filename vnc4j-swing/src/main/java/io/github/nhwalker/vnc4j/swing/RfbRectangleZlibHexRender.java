package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.HextileSubrect;
import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleZlibHex;
import io.github.nhwalker.vnc4j.protocol.ZlibHexTile;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Renders {@link RfbRectangleZlibHex} rectangles (encoding type 8) onto a
 * {@link BufferedImage}.
 *
 * <p>ZlibHex extends Hextile with two additional subencoding bits:
 * <ul>
 *   <li>{@link ZlibHexTile#SUBENC_ZLIB_RAW} – tile raw pixel data is zlib-compressed</li>
 *   <li>{@link ZlibHexTile#SUBENC_ZLIB} – tile subrect data is zlib-compressed</li>
 * </ul>
 *
 * <p>Two <em>persistent zlib streams</em> are maintained: one for raw tile data
 * ({@link ZlibHexTile#SUBENC_ZLIB_RAW}) and one for subrect data
 * ({@link ZlibHexTile#SUBENC_ZLIB}). Both streams survive across rectangles for
 * the lifetime of the connection.
 */
public final class RfbRectangleZlibHexRender implements RfbRectangleRender<RfbRectangleZlibHex> {

    private final PixelFormat pixelFormat;
    private final Inflater rawStream    = new Inflater();
    private final Inflater subrectStream = new Inflater();

    public RfbRectangleZlibHexRender(PixelFormat pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    @Override
    public void render(RfbRectangleZlibHex rectangle, BufferedImage image) {
        List<ZlibHexTile> tiles = rectangle.tiles();
        int tileIdx = 0;

        int bg = 0xFF000000;
        int fg = 0xFF000000;

        int cols = (rectangle.width() + 15) / 16;
        int rows = (rectangle.height() + 15) / 16;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (tileIdx >= tiles.size()) break;
                ZlibHexTile tile = tiles.get(tileIdx++);

                int tileX = rectangle.x() + col * 16;
                int tileY = rectangle.y() + row * 16;
                int tileW = Math.min(16, rectangle.x() + rectangle.width() - tileX);
                int tileH = Math.min(16, rectangle.y() + rectangle.height() - tileY);

                int subenc = tile.subencoding();

                if ((subenc & ZlibHexTile.SUBENC_ZLIB_RAW) != 0) {
                    int bpp = PixelDecoder.bytesPerPixel(pixelFormat);
                    byte[] raw = PixelDecoder.inflate(rawStream, tile.zlibRawData());
                    PixelDecoder.drawRawPixels(image, tileX, tileY, tileW, tileH, raw, 0, pixelFormat);
                    continue;
                }

                if ((subenc & ZlibHexTile.SUBENC_BACKGROUND_SPECIFIED) != 0) {
                    bg = PixelDecoder.decodePixel(tile.background(), 0, pixelFormat);
                }
                PixelDecoder.fillRect(image, tileX, tileY, tileW, tileH, bg);

                if ((subenc & ZlibHexTile.SUBENC_FOREGROUND_SPECIFIED) != 0) {
                    fg = PixelDecoder.decodePixel(tile.foreground(), 0, pixelFormat);
                }

                if ((subenc & ZlibHexTile.SUBENC_ANY_SUBRECTS) != 0) {
                    boolean coloured = (subenc & ZlibHexTile.SUBENC_SUBRECTS_COLOURED) != 0;

                    if ((subenc & ZlibHexTile.SUBENC_ZLIB) != 0) {
                        // Subrect data is zlib-compressed; decompress and parse manually.
                        byte[] subrectData = PixelDecoder.inflate(subrectStream, tile.zlibSubrectData());
                        renderZlibSubrects(image, subrectData, tileX, tileY, fg, coloured);
                    } else {
                        for (HextileSubrect sub : tile.subrects()) {
                            int argb = coloured
                                    ? PixelDecoder.decodePixel(sub.pixel(), 0, pixelFormat)
                                    : fg;
                            PixelDecoder.fillRect(image,
                                    tileX + sub.x(), tileY + sub.y(),
                                    sub.width(), sub.height(), argb);
                        }
                    }
                }
            }
        }
    }

    /**
     * Parses Hextile subrect records from a raw decompressed byte array and draws
     * them. Each subrect is: [optional pixel bytes] + 1 packed-xy byte + 1 packed-wh byte.
     */
    private void renderZlibSubrects(BufferedImage image, byte[] data,
            int tileX, int tileY, int fg, boolean coloured) {
        int bpp = PixelDecoder.bytesPerPixel(pixelFormat);
        int pos = 0;
        while (pos < data.length) {
            int argb = fg;
            if (coloured) {
                if (pos + bpp > data.length) break;
                argb = PixelDecoder.decodePixel(data, pos, pixelFormat);
                pos += bpp;
            }
            if (pos + 2 > data.length) break;
            int xy = data[pos++] & 0xFF;
            int wh = data[pos++] & 0xFF;
            int sx = (xy >> 4) & 0xF;
            int sy = xy & 0xF;
            int sw = ((wh >> 4) & 0xF) + 1;
            int sh = (wh & 0xF) + 1;
            PixelDecoder.fillRect(image, tileX + sx, tileY + sy, sw, sh, argb);
        }
    }
}
