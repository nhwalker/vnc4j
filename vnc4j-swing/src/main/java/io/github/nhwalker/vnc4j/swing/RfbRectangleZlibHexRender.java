package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.HextileSubrect;
import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleZlibHex;
import io.github.nhwalker.vnc4j.protocol.ZlibHexTile;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Renders an {@link RfbRectangleZlibHex} (encoding type 8) onto a {@link BufferedImage}.
 *
 * <p>ZlibHex extends Hextile with two additional subencoding bits:
 * <ul>
 *   <li>{@link ZlibHexTile#SUBENC_ZLIB_RAW} – the entire tile raw data is zlib-compressed</li>
 *   <li>{@link ZlibHexTile#SUBENC_ZLIB} – the subrect portion is zlib-compressed</li>
 * </ul>
 * All other bits follow the standard Hextile semantics.
 *
 * <p><b>Note:</b> ZlibHex uses two persistent zlib streams shared across tiles and
 * rectangles. This renderer decompresses each chunk independently (fresh stream),
 * which is only correct for the very first use of each stream.
 */
public final class RfbRectangleZlibHexRender implements RfbRectangleRender {

    private final RfbRectangleZlibHex rectangle;
    private final PixelFormat pixelFormat;

    public RfbRectangleZlibHexRender(RfbRectangleZlibHex rectangle, PixelFormat pixelFormat) {
        this.rectangle = rectangle;
        this.pixelFormat = pixelFormat;
    }

    @Override
    public void render(BufferedImage image) {
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
                    // Entire tile is zlib-compressed raw pixels
                    int bpp = PixelDecoder.bytesPerPixel(pixelFormat);
                    byte[] raw = inflate(tile.zlibRawData(), tileW * tileH * bpp);
                    PixelDecoder.drawRawPixels(image, tileX, tileY, tileW, tileH, raw, 0, pixelFormat);
                    continue;
                }

                if ((subenc & ZlibHexTile.SUBENC_RAW) != 0) {
                    // Raw pixels (not zlib-compressed)
                    // rawPixels is not a field of ZlibHexTile; this case is covered by ZLIB_RAW above.
                    // If RAW without ZLIB_RAW, the data should have been stored elsewhere; skip.
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
                    List<HextileSubrect> subrects;

                    if ((subenc & ZlibHexTile.SUBENC_ZLIB) != 0) {
                        // Subrect data is zlib-compressed; we cannot parse it without the
                        // protocol-level stream state, so skip subrect rendering.
                        continue;
                    } else {
                        subrects = tile.subrects();
                    }

                    for (HextileSubrect sub : subrects) {
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

    private static byte[] inflate(byte[] compressed, int expectedSize) {
        if (compressed == null || compressed.length == 0) return new byte[expectedSize];
        byte[] out = new byte[expectedSize];
        Inflater inf = new Inflater();
        try {
            inf.setInput(compressed);
            inf.inflate(out);
        } catch (DataFormatException e) {
            throw new IllegalStateException("Failed to decompress ZlibHex tile data", e);
        } finally {
            inf.end();
        }
        return out;
    }
}
