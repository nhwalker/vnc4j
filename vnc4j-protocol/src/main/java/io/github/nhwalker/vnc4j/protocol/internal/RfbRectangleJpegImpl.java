package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.messages.RfbRectangleJpeg;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public final class RfbRectangleJpegImpl implements RfbRectangleJpeg {
    private final int x, y, width, height;
    private final byte[] data;

    public RfbRectangleJpegImpl(int x, int y, int width, int height, byte[] data) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.data = data;
    }

    @Override public int x() { return x; }
    @Override public int y() { return y; }
    @Override public int width() { return width; }
    @Override public int height() { return height; }
    @Override public byte[] data() { return data; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RfbRectangleJpeg r)) return false;
        return x == r.x() && y == r.y() && width == r.width() && height == r.height()
                && Arrays.equals(data, r.data());
    }

    @Override public int hashCode() { return Objects.hash(x, y, width, height, Arrays.hashCode(data)); }
    @Override public String toString() {
        return "RfbRectangleJpeg[x=" + x + ", y=" + y + ", width=" + width + ", height=" + height
                + ", data.len=" + (data != null ? data.length : "null") + "]";
    }

    @Override
    public void write(java.io.OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeShort(x); dos.writeShort(y); dos.writeShort(width); dos.writeShort(height);
        dos.writeInt(ENCODING_TYPE);
        if (data != null) dos.write(data);
    }

    public static final class BuilderImpl implements RfbRectangleJpeg.Builder {
        private int x, y, width, height;
        private byte[] data;

        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }
        @Override public Builder data(byte[] v) { this.data = v; return this; }

        @Override public RfbRectangleJpeg build() {
            return new RfbRectangleJpegImpl(x, y, width, height, data);
        }
    }
}
