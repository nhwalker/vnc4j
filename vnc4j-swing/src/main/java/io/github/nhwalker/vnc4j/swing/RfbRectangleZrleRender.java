package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.messages.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.messages.RfbRectangleZrle;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.Inflater;

/**
 * Renders {@link RfbRectangleZrle} rectangles (encoding type 16) onto a
 * {@link BufferedImage}.
 *
 * <p>ZRLE uses a <em>single persistent zlib stream</em> across all rectangles.
 * This renderer maintains that stream as a field so the decompressor's context
 * is preserved between calls to {@link #render}.
 *
 * <p>Each rectangle is divided into 64×64 tiles, each prefixed by a subencoding byte:
 * <ul>
 *   <li>0 – Raw: one CPIXEL per pixel</li>
 *   <li>1 – Solid: fill tile with one CPIXEL</li>
 *   <li>2–16 – Packed palette: palette of N CPIXELs then packed bit indices</li>
 *   <li>128 – Plain RLE: (CPIXEL, run-length) pairs</li>
 *   <li>130–255 – Palette RLE: palette of (subencoding − 128) CPIXELs then RLE</li>
 * </ul>
 */
public final class RfbRectangleZrleRender implements RfbRectangleRender<RfbRectangleZrle> {

    private final PixelFormat pixelFormat;
    private final Inflater inflater = new Inflater();

    public RfbRectangleZrleRender(PixelFormat pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    @Override
    public void render(RfbRectangleZrle rectangle, BufferedImage image) {
        byte[] compressed = rectangle.zlibData();
        if (compressed == null || compressed.length == 0) return;

        byte[] raw = PixelDecoder.inflate(inflater, compressed);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(raw));

        int cpxSize = PixelDecoder.cpixelSize(pixelFormat);

        try {
            int cols = (rectangle.width() + 63) / 64;
            int rows = (rectangle.height() + 63) / 64;

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    int tileX = rectangle.x() + col * 64;
                    int tileY = rectangle.y() + row * 64;
                    int tileW = Math.min(64, rectangle.x() + rectangle.width()  - tileX);
                    int tileH = Math.min(64, rectangle.y() + rectangle.height() - tileY);

                    int subenc = in.readUnsignedByte();

                    if (subenc == 0) {
                        renderRaw(image, in, tileX, tileY, tileW, tileH, cpxSize);
                    } else if (subenc == 1) {
                        renderSolid(image, in, tileX, tileY, tileW, tileH, cpxSize);
                    } else if (subenc >= 2 && subenc <= 16) {
                        renderPackedPalette(image, in, tileX, tileY, tileW, tileH, subenc, cpxSize);
                    } else if (subenc == 128) {
                        renderPlainRle(image, in, tileX, tileY, tileW, tileH, cpxSize);
                    } else if (subenc >= 130) {
                        renderPaletteRle(image, in, tileX, tileY, tileW, tileH, subenc - 128, cpxSize);
                    }
                    // subenc 17–127 and 129 are undefined; skip
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read decompressed ZRLE data", e);
        }
    }

    private void renderRaw(BufferedImage image, DataInputStream in,
            int tileX, int tileY, int tileW, int tileH, int cpxSize) throws IOException {
        byte[] buf = new byte[cpxSize * tileW * tileH];
        in.readFully(buf);
        int[] argb = new int[tileW * tileH];
        for (int i = 0; i < argb.length; i++) {
            argb[i] = PixelDecoder.decodeCPixel(buf, i * cpxSize, pixelFormat);
        }
        image.setRGB(tileX, tileY, tileW, tileH, argb, 0, tileW);
    }

    private void renderSolid(BufferedImage image, DataInputStream in,
            int tileX, int tileY, int tileW, int tileH, int cpxSize) throws IOException {
        byte[] buf = new byte[cpxSize];
        in.readFully(buf);
        PixelDecoder.fillRect(image, tileX, tileY, tileW, tileH,
                PixelDecoder.decodeCPixel(buf, 0, pixelFormat));
    }

    private void renderPackedPalette(BufferedImage image, DataInputStream in,
            int tileX, int tileY, int tileW, int tileH,
            int paletteSize, int cpxSize) throws IOException {
        int[] palette = readPalette(in, paletteSize, cpxSize);
        int bitsPerIdx = bitsForPaletteSize(paletteSize);
        int mask = (1 << bitsPerIdx) - 1;

        int[] argb = new int[tileW * tileH];
        int pixIdx = 0;
        for (int dy = 0; dy < tileH; dy++) {
            // Per the spec, each row is padded to a whole number of bytes, so the
            // bit accumulator must be reset at the start of every row.
            int accum = 0;
            int bitsLeft = 0;
            for (int dx = 0; dx < tileW; dx++) {
                if (bitsLeft < bitsPerIdx) {
                    accum = (accum << 8) | in.readUnsignedByte();
                    bitsLeft += 8;
                }
                bitsLeft -= bitsPerIdx;
                int idx = (accum >> bitsLeft) & mask;
                argb[pixIdx++] = (idx < palette.length) ? palette[idx] : 0xFF000000;
            }
        }
        image.setRGB(tileX, tileY, tileW, tileH, argb, 0, tileW);
    }

    private void renderPlainRle(BufferedImage image, DataInputStream in,
            int tileX, int tileY, int tileW, int tileH, int cpxSize) throws IOException {
        byte[] buf = new byte[cpxSize];
        int[] tile = new int[tileW * tileH];
        int pos = 0;
        while (pos < tile.length) {
            in.readFully(buf);
            int argb = PixelDecoder.decodeCPixel(buf, 0, pixelFormat);
            int run = readRunLength(in);
            for (int i = 0; i < run && pos < tile.length; i++) tile[pos++] = argb;
        }
        image.setRGB(tileX, tileY, tileW, tileH, tile, 0, tileW);
    }

    private void renderPaletteRle(BufferedImage image, DataInputStream in,
            int tileX, int tileY, int tileW, int tileH,
            int paletteSize, int cpxSize) throws IOException {
        int[] palette = readPalette(in, paletteSize, cpxSize);
        int[] tile = new int[tileW * tileH];
        int pos = 0;
        while (pos < tile.length) {
            int idxByte = in.readUnsignedByte();
            boolean isRun = (idxByte & 0x80) != 0;
            int idx = idxByte & 0x7F;
            int argb = (idx < palette.length) ? palette[idx] : 0xFF000000;
            int run = isRun ? readRunLength(in) : 1;
            for (int i = 0; i < run && pos < tile.length; i++) tile[pos++] = argb;
        }
        image.setRGB(tileX, tileY, tileW, tileH, tile, 0, tileW);
    }

    private int[] readPalette(DataInputStream in, int size, int cpxSize) throws IOException {
        int[] palette = new int[size];
        byte[] buf = new byte[cpxSize];
        for (int i = 0; i < size; i++) {
            in.readFully(buf);
            palette[i] = PixelDecoder.decodeCPixel(buf, 0, pixelFormat);
        }
        return palette;
    }

    private static int readRunLength(DataInputStream in) throws IOException {
        int run = 1;
        int b;
        do { b = in.readUnsignedByte(); run += b; } while (b == 255);
        return run;
    }

    private static int bitsForPaletteSize(int n) {
        if (n <= 2) return 1;
        if (n <= 4) return 2;
        return 4;
    }
}
