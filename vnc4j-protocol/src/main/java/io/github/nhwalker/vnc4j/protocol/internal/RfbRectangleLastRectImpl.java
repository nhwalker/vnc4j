package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.messages.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.messages.RfbRectangleLastRect;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public final class RfbRectangleLastRectImpl implements RfbRectangleLastRect {
    private final int x, y, width, height;

    public RfbRectangleLastRectImpl(int x, int y, int width, int height) {
        this.x = x; this.y = y; this.width = width; this.height = height;
    }

    @Override public int x() { return x; }
    @Override public int y() { return y; }
    @Override public int width() { return width; }
    @Override public int height() { return height; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RfbRectangleLastRect r)) return false;
        return x == r.x() && y == r.y() && width == r.width() && height == r.height();
    }

    @Override public int hashCode() { return Objects.hash(x, y, width, height); }
    @Override public String toString() {
        return "RfbRectangleLastRect[x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
    }

    @Override
    public void write(java.io.OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeShort(x); dos.writeShort(y); dos.writeShort(width); dos.writeShort(height);
        dos.writeInt(ENCODING_TYPE);
    }

    public static RfbRectangleLastRect readPayload(DataInputStream dis, int x, int y, int w, int h,
            PixelFormat pf) throws IOException {
        return new RfbRectangleLastRectImpl(x, y, w, h);
    }

    public static final class BuilderImpl implements RfbRectangleLastRect.Builder {
        private int x, y, width, height;

        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }

        @Override public RfbRectangleLastRect build() {
            return new RfbRectangleLastRectImpl(x, y, width, height);
        }
    }
}
