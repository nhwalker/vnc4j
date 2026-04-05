package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleZrle;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Renders an {@link RfbRectangleZrle} (encoding type 16) onto a {@link BufferedImage}.
 *
 * <p>The rectangle is divided into 64×64 tiles. Each tile is preceded by a single
 * subencoding byte that selects one of the following modes:
 * <ul>
 *   <li>0 – Raw: one CPIXEL per pixel</li>
 *   <li>1 – Solid: fill tile with one CPIXEL</li>
 *   <li>2–16 – Packed palette: palette of N CPIXELs followed by packed bit indices</li>
 *   <li>128 – Plain RLE: (CPIXEL, run-length) pairs until tile is filled</li>
 *   <li>130–255 – Palette RLE: palette of (subencoding − 128) CPIXELs, then RLE
 *       with palette indices</li>
 * </ul>
 */
public final class RfbRectangleZrleRender implements RfbRectangleRender {

    private final RfbRectangleZrle rectangle;
    private final PixelFormat pixelFormat;

    public RfbRectangleZrleRender(RfbRectangleZrle rectangle, PixelFormat pixelFormat) {
        this.rectangle = rectangle;
        this.pixelFormat = pixelFormat;
    }

    @Override
    public void render(BufferedImage image) {
        byte[] compressed = rectangle.zlibData();
        if (compressed == null || compressed.length == 0) return;

        byte[] raw = decompress(compressed);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(raw));

        int cpxSize = PixelDecoder.cpixelSize(pixelFormat);

        try {
            int cols = (rectangle.width() + 63) / 64;
            int rows = (rectangle.height() + 63) / 64;

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    int tileX = rectangle.x() + col * 64;
                    int tileY = rectangle.y() + row * 64;
                    int tileW = Math.min(64, rectangle.x() + rectangle.width() - tileX);
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
        byte[] buf = new byte[cpxSize];
        int[] argb = new int[tileW];
        for (int dy = 0; dy < tileH; dy++) {
            for (int dx = 0; dx < tileW; dx++) {
                in.readFully(buf);
                argb[dx] = PixelDecoder.decodeCPixel(buf, 0, pixelFormat);
            }
            image.setRGB(tileX, tileY + dy, tileW, 1, argb, 0, tileW);
        }
    }

    private void renderSolid(BufferedImage image, DataInputStream in,
            int tileX, int tileY, int tileW, int tileH, int cpxSize) throws IOException {
        byte[] buf = new byte[cpxSize];
        in.readFully(buf);
        int argb = PixelDecoder.decodeCPixel(buf, 0, pixelFormat);
        PixelDecoder.fillRect(image, tileX, tileY, tileW, tileH, argb);
    }

    private void renderPackedPalette(BufferedImage image, DataInputStream in,
            int tileX, int tileY, int tileW, int tileH,
            int paletteSize, int cpxSize) throws IOException {
        // Read palette
        int[] palette = readPalette(in, paletteSize, cpxSize);

        // Bits per palette index
        int bitsPerIdx = bitsForPaletteSize(paletteSize);
        int mask = (1 << bitsPerIdx) - 1;

        int[] argb = new int[tileW];
        for (int dy = 0; dy < tileH; dy++) {
            int accum = 0;
            int bitsLeft = 0;
            for (int dx = 0; dx < tileW; dx++) {
                if (bitsLeft < bitsPerIdx) {
                    accum = (accum << 8) | in.readUnsignedByte();
                    bitsLeft += 8;
                }
                bitsLeft -= bitsPerIdx;
                int idx = (accum >> bitsLeft) & mask;
                argb[dx] = (idx < palette.length) ? palette[idx] : 0xFF000000;
            }
            // Consume remaining bits to align to byte boundary
            // (bitsLeft is already < bitsPerIdx; row padding is implicit)
            image.setRGB(tileX, tileY + dy, tileW, 1, argb, 0, tileW);
        }
    }

    private void renderPlainRle(BufferedImage image, DataInputStream in,
            int tileX, int tileY, int tileW, int tileH, int cpxSize) throws IOException {
        byte[] buf = new byte[cpxSize];
        int total = tileW * tileH;
        int pos = 0;
        int[] tile = new int[total];

        while (pos < total) {
            in.readFully(buf);
            int argb = PixelDecoder.decodeCPixel(buf, 0, pixelFormat);
            int run = readRunLength(in);
            for (int i = 0; i < run && pos < total; i++) {
                tile[pos++] = argb;
            }
        }
        image.setRGB(tileX, tileY, tileW, tileH, tile, 0, tileW);
    }

    private void renderPaletteRle(BufferedImage image, DataInputStream in,
            int tileX, int tileY, int tileW, int tileH,
            int paletteSize, int cpxSize) throws IOException {
        int[] palette = readPalette(in, paletteSize, cpxSize);

        int total = tileW * tileH;
        int pos = 0;
        int[] tile = new int[total];

        while (pos < total) {
            int idxByte = in.readUnsignedByte();
            boolean isRun = (idxByte & 0x80) != 0;
            int idx = idxByte & 0x7F;
            int argb = (idx < palette.length) ? palette[idx] : 0xFF000000;

            int run;
            if (isRun) {
                run = readRunLength(in);
            } else {
                run = 1;
            }
            for (int i = 0; i < run && pos < total; i++) {
                tile[pos++] = argb;
            }
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

    /**
     * Reads a variable-length ZRLE run length. The run length is 1 plus the
     * sum of all bytes read; reading stops when a byte less than 255 is encountered.
     */
    private static int readRunLength(DataInputStream in) throws IOException {
        int run = 1;
        int b;
        do {
            b = in.readUnsignedByte();
            run += b;
        } while (b == 255);
        return run;
    }

    private static int bitsForPaletteSize(int n) {
        if (n <= 2) return 1;
        if (n <= 4) return 2;
        return 4; // n <= 16
    }

    private static byte[] decompress(byte[] compressed) {
        // Decompress into a buffer sized generously; ZRLE decompressed data
        // is at most 64 * 64 * 4 bytes per tile, but we do not know the tile
        // count upfront. Use an expanding approach.
        byte[] out = new byte[compressed.length * 8 + 4096];
        Inflater inf = new Inflater();
        inf.setInput(compressed);
        int written = 0;
        try {
            while (!inf.finished()) {
                if (written >= out.length) {
                    byte[] bigger = new byte[out.length * 2];
                    System.arraycopy(out, 0, bigger, 0, written);
                    out = bigger;
                }
                written += inf.inflate(out, written, out.length - written);
            }
        } catch (DataFormatException e) {
            throw new IllegalStateException("Failed to decompress ZRLE rectangle data", e);
        } finally {
            inf.end();
        }
        byte[] result = new byte[written];
        System.arraycopy(out, 0, result, 0, written);
        return result;
    }
}
