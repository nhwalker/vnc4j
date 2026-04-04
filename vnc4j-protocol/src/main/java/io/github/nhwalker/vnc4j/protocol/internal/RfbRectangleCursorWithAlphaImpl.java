package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleCursorWithAlpha;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public final class RfbRectangleCursorWithAlphaImpl implements RfbRectangleCursorWithAlpha {
    private final int x, y, width, height;
    private final int encoding;
    private final byte[] data;

    public RfbRectangleCursorWithAlphaImpl(int x, int y, int width, int height, int encoding, byte[] data) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.encoding = encoding; this.data = data;
    }

    @Override public int x() { return x; }
    @Override public int y() { return y; }
    @Override public int width() { return width; }
    @Override public int height() { return height; }
    @Override public int encoding() { return encoding; }
    @Override public byte[] data() { return data; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RfbRectangleCursorWithAlpha r)) return false;
        return x == r.x() && y == r.y() && width == r.width() && height == r.height()
                && encoding == r.encoding() && Arrays.equals(data, r.data());
    }

    @Override public int hashCode() {
        return Objects.hash(x, y, width, height, encoding, Arrays.hashCode(data));
    }
    @Override public String toString() {
        return "RfbRectangleCursorWithAlpha[x=" + x + ", y=" + y + ", width=" + width
                + ", height=" + height + ", encoding=" + encoding
                + ", data.len=" + (data != null ? data.length : "null") + "]";
    }

    @Override
    public void write(java.io.OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeShort(x); dos.writeShort(y); dos.writeShort(width); dos.writeShort(height);
        dos.writeInt(ENCODING_TYPE);
        dos.writeInt(encoding);
        if (data != null) dos.write(data);
    }

    public static RfbRectangleCursorWithAlpha readPayload(DataInputStream dis, int x, int y, int w, int h,
            PixelFormat pf) throws IOException {
        int encoding = dis.readInt();
        // Pixel data is always 32-bit RGBA (4 bytes per pixel)
        byte[] data = new byte[w * h * 4];
        dis.readFully(data);
        return new RfbRectangleCursorWithAlphaImpl(x, y, w, h, encoding, data);
    }

    public static final class BuilderImpl implements RfbRectangleCursorWithAlpha.Builder {
        private int x, y, width, height, encoding;
        private byte[] data;

        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }
        @Override public Builder encoding(int v) { this.encoding = v; return this; }
        @Override public Builder data(byte[] v) { this.data = v; return this; }

        @Override public RfbRectangleCursorWithAlpha build() {
            return new RfbRectangleCursorWithAlphaImpl(x, y, width, height, encoding, data);
        }
    }
}
