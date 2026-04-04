package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleZrle;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public final class RfbRectangleZrleImpl implements RfbRectangleZrle {
    private final int x, y, width, height;
    private final byte[] zlibData;

    public RfbRectangleZrleImpl(int x, int y, int width, int height, byte[] zlibData) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.zlibData = zlibData;
    }

    @Override public int x() { return x; }
    @Override public int y() { return y; }
    @Override public int width() { return width; }
    @Override public int height() { return height; }
    @Override public byte[] zlibData() { return zlibData; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RfbRectangleZrle r)) return false;
        return x == r.x() && y == r.y() && width == r.width() && height == r.height()
                && Arrays.equals(zlibData, r.zlibData());
    }

    @Override public int hashCode() { return Objects.hash(x, y, width, height, Arrays.hashCode(zlibData)); }
    @Override public String toString() {
        return "RfbRectangleZrle[x=" + x + ", y=" + y + ", width=" + width + ", height=" + height
                + ", zlibData.len=" + (zlibData != null ? zlibData.length : "null") + "]";
    }

    @Override
    public void write(java.io.OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeShort(x); dos.writeShort(y); dos.writeShort(width); dos.writeShort(height);
        dos.writeInt(ENCODING_TYPE);
        byte[] data = zlibData != null ? zlibData : new byte[0];
        dos.writeInt(data.length);
        dos.write(data);
    }

    public static RfbRectangleZrle readPayload(DataInputStream dis, int x, int y, int w, int h,
            PixelFormat pf) throws IOException {
        int len = dis.readInt();
        byte[] data = new byte[len];
        dis.readFully(data);
        return new RfbRectangleZrleImpl(x, y, w, h, data);
    }

    public static final class BuilderImpl implements RfbRectangleZrle.Builder {
        private int x, y, width, height;
        private byte[] zlibData;

        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }
        @Override public Builder zlibData(byte[] v) { this.zlibData = v; return this; }

        @Override public RfbRectangleZrle build() {
            return new RfbRectangleZrleImpl(x, y, width, height, zlibData);
        }
    }
}
