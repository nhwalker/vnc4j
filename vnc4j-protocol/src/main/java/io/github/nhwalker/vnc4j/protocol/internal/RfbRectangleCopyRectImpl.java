package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.messages.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.messages.RfbRectangleCopyRect;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public final class RfbRectangleCopyRectImpl implements RfbRectangleCopyRect {
    private final int x, y, width, height, srcX, srcY;

    public RfbRectangleCopyRectImpl(int x, int y, int width, int height, int srcX, int srcY) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.srcX = srcX; this.srcY = srcY;
    }

    @Override public int x() { return x; }
    @Override public int y() { return y; }
    @Override public int width() { return width; }
    @Override public int height() { return height; }
    @Override public int srcX() { return srcX; }
    @Override public int srcY() { return srcY; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RfbRectangleCopyRect r)) return false;
        return x == r.x() && y == r.y() && width == r.width() && height == r.height()
                && srcX == r.srcX() && srcY == r.srcY();
    }

    @Override public int hashCode() { return Objects.hash(x, y, width, height, srcX, srcY); }
    @Override public String toString() {
        return "RfbRectangleCopyRect[x=" + x + ", y=" + y + ", width=" + width + ", height=" + height
                + ", srcX=" + srcX + ", srcY=" + srcY + "]";
    }

    @Override
    public void write(java.io.OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeShort(x); dos.writeShort(y); dos.writeShort(width); dos.writeShort(height);
        dos.writeInt(ENCODING_TYPE);
        dos.writeShort(srcX); dos.writeShort(srcY);
    }

    public static RfbRectangleCopyRect readPayload(DataInputStream dis, int x, int y, int w, int h,
            PixelFormat pf) throws IOException {
        int srcX = dis.readUnsignedShort();
        int srcY = dis.readUnsignedShort();
        return new RfbRectangleCopyRectImpl(x, y, w, h, srcX, srcY);
    }

    public static final class BuilderImpl implements RfbRectangleCopyRect.Builder {
        private int x, y, width, height, srcX, srcY;

        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }
        @Override public Builder srcX(int v) { this.srcX = v; return this; }
        @Override public Builder srcY(int v) { this.srcY = v; return this; }

        @Override public RfbRectangleCopyRect build() {
            return new RfbRectangleCopyRectImpl(x, y, width, height, srcX, srcY);
        }
    }
}
