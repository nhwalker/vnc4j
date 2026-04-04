package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleRaw;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

public final class RfbRectangleRawImpl implements RfbRectangleRaw {
    private final int x, y, width, height;
    private final byte[] pixels;

    public RfbRectangleRawImpl(int x, int y, int width, int height, byte[] pixels) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.pixels = pixels;
    }

    @Override public int x() { return x; }
    @Override public int y() { return y; }
    @Override public int width() { return width; }
    @Override public int height() { return height; }
    @Override public byte[] pixels() { return pixels; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RfbRectangleRaw r)) return false;
        return x == r.x() && y == r.y() && width == r.width() && height == r.height()
                && Arrays.equals(pixels, r.pixels());
    }

    @Override public int hashCode() { return Objects.hash(x, y, width, height, Arrays.hashCode(pixels)); }
    @Override public String toString() {
        return "RfbRectangleRaw[x=" + x + ", y=" + y + ", width=" + width + ", height=" + height
                + ", pixels.len=" + (pixels != null ? pixels.length : "null") + "]";
    }

    @Override
    public void write(java.io.OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeShort(x); dos.writeShort(y); dos.writeShort(width); dos.writeShort(height);
        dos.writeInt(ENCODING_TYPE);
        if (pixels != null) dos.write(pixels);
    }

    public static RfbRectangleRaw readPayload(DataInputStream dis, int x, int y, int w, int h,
            PixelFormat pf) throws IOException {
        int bpp = pf.bitsPerPixel() / 8;
        byte[] pixels = new byte[w * h * bpp];
        dis.readFully(pixels);
        return new RfbRectangleRawImpl(x, y, w, h, pixels);
    }

    public static final class BuilderImpl implements RfbRectangleRaw.Builder {
        private int x, y, width, height;
        private byte[] pixels;

        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }
        @Override public Builder pixels(byte[] v) { this.pixels = v; return this; }

        @Override public RfbRectangleRaw build() { return new RfbRectangleRawImpl(x, y, width, height, pixels); }
    }
}
