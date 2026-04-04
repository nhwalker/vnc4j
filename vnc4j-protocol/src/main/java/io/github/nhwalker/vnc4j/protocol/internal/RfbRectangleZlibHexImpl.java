package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleZlibHex;
import io.github.nhwalker.vnc4j.protocol.ZlibHexTile;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RfbRectangleZlibHexImpl implements RfbRectangleZlibHex {
    private final int x, y, width, height;
    private final List<ZlibHexTile> tiles;

    public RfbRectangleZlibHexImpl(int x, int y, int width, int height, List<ZlibHexTile> tiles) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.tiles = tiles != null ? tiles : List.of();
    }

    @Override public int x() { return x; }
    @Override public int y() { return y; }
    @Override public int width() { return width; }
    @Override public int height() { return height; }
    @Override public List<ZlibHexTile> tiles() { return tiles; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RfbRectangleZlibHex r)) return false;
        return x == r.x() && y == r.y() && width == r.width() && height == r.height()
                && Objects.equals(tiles, r.tiles());
    }

    @Override public int hashCode() { return Objects.hash(x, y, width, height, tiles); }
    @Override public String toString() {
        return "RfbRectangleZlibHex[x=" + x + ", y=" + y + ", width=" + width + ", height=" + height
                + ", tiles.size=" + tiles.size() + "]";
    }

    @Override
    public void write(java.io.OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeShort(x); dos.writeShort(y); dos.writeShort(width); dos.writeShort(height);
        dos.writeInt(ENCODING_TYPE);
        for (ZlibHexTile tile : tiles) {
            tile.write(out);
        }
    }

    public static RfbRectangleZlibHex readPayload(DataInputStream dis, int x, int y, int w, int h,
            PixelFormat pf) throws IOException {
        int bpp = pf.bitsPerPixel() / 8;
        int tilesX = (w + TILE_SIZE - 1) / TILE_SIZE;
        int tilesY = (h + TILE_SIZE - 1) / TILE_SIZE;
        List<ZlibHexTile> tiles = new ArrayList<>(tilesX * tilesY);
        for (int ty = 0; ty < tilesY; ty++) {
            for (int tx = 0; tx < tilesX; tx++) {
                int tileW = Math.min(TILE_SIZE, w - tx * TILE_SIZE);
                int tileH = Math.min(TILE_SIZE, h - ty * TILE_SIZE);
                tiles.add(ZlibHexTile.read(dis, tileW, tileH, bpp));
            }
        }
        return new RfbRectangleZlibHexImpl(x, y, w, h, tiles);
    }

    public static final class BuilderImpl implements RfbRectangleZlibHex.Builder {
        private int x, y, width, height;
        private List<ZlibHexTile> tiles;

        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }
        @Override public Builder tiles(List<ZlibHexTile> v) { this.tiles = v; return this; }

        @Override public RfbRectangleZlibHex build() {
            return new RfbRectangleZlibHexImpl(x, y, width, height, tiles);
        }
    }
}
