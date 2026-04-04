package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleH264;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public final class RfbRectangleH264Impl implements RfbRectangleH264 {
    private final int x, y, width, height;
    private final int flags;
    private final byte[] data;

    public RfbRectangleH264Impl(int x, int y, int width, int height, int flags, byte[] data) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.flags = flags; this.data = data;
    }

    @Override public int x() { return x; }
    @Override public int y() { return y; }
    @Override public int width() { return width; }
    @Override public int height() { return height; }
    @Override public int flags() { return flags; }
    @Override public byte[] data() { return data; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RfbRectangleH264 r)) return false;
        return x == r.x() && y == r.y() && width == r.width() && height == r.height()
                && flags == r.flags() && Arrays.equals(data, r.data());
    }

    @Override public int hashCode() { return Objects.hash(x, y, width, height, flags, Arrays.hashCode(data)); }
    @Override public String toString() {
        return "RfbRectangleH264[x=" + x + ", y=" + y + ", width=" + width + ", height=" + height
                + ", flags=" + flags + ", data.len=" + (data != null ? data.length : "null") + "]";
    }

    @Override
    public void write(java.io.OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeShort(x); dos.writeShort(y); dos.writeShort(width); dos.writeShort(height);
        dos.writeInt(ENCODING_TYPE);
        byte[] d = data != null ? data : new byte[0];
        dos.writeInt(d.length);
        dos.writeInt(flags);
        dos.write(d);
    }

    public static RfbRectangleH264 readPayload(DataInputStream dis, int x, int y, int w, int h,
            PixelFormat pf) throws IOException {
        int len = dis.readInt();
        int flags = dis.readInt();
        byte[] data = new byte[len];
        dis.readFully(data);
        return new RfbRectangleH264Impl(x, y, w, h, flags, data);
    }

    public static final class BuilderImpl implements RfbRectangleH264.Builder {
        private int x, y, width, height, flags;
        private byte[] data;

        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }
        @Override public Builder flags(int v) { this.flags = v; return this; }
        @Override public Builder data(byte[] v) { this.data = v; return this; }

        @Override public RfbRectangleH264 build() {
            return new RfbRectangleH264Impl(x, y, width, height, flags, data);
        }
    }
}
