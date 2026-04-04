package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleTightFill;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public final class RfbRectangleTightFillImpl implements RfbRectangleTightFill {
    private final int x, y, width, height;
    private final int streamResets;
    private final byte[] fillColor;

    public RfbRectangleTightFillImpl(int x, int y, int width, int height,
            int streamResets, byte[] fillColor) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.streamResets = streamResets; this.fillColor = fillColor;
    }

    @Override public int x() { return x; }
    @Override public int y() { return y; }
    @Override public int width() { return width; }
    @Override public int height() { return height; }
    @Override public int streamResets() { return streamResets; }
    @Override public byte[] fillColor() { return fillColor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RfbRectangleTightFill r)) return false;
        return x == r.x() && y == r.y() && width == r.width() && height == r.height()
                && streamResets == r.streamResets() && Arrays.equals(fillColor, r.fillColor());
    }

    @Override public int hashCode() {
        return Objects.hash(x, y, width, height, streamResets, Arrays.hashCode(fillColor));
    }
    @Override public String toString() {
        return "RfbRectangleTightFill[x=" + x + ", y=" + y + ", width=" + width
                + ", height=" + height + ", streamResets=" + streamResets + "]";
    }

    @Override
    public void write(java.io.OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeShort(x); dos.writeShort(y); dos.writeShort(width); dos.writeShort(height);
        dos.writeInt(ENCODING_TYPE);
        dos.writeByte((streamResets & 0xF) | 0x80); // FillCompression = 0x8_
        if (fillColor != null) dos.write(fillColor);
    }

    public static RfbRectangleTightFill readPayload(DataInputStream dis, int x, int y, int w, int h,
            PixelFormat pf, int streamResets) throws IOException {
        int tpixelSize = TightIo.tpixelSize(pf);
        byte[] fillColor = new byte[tpixelSize];
        dis.readFully(fillColor);
        return new RfbRectangleTightFillImpl(x, y, w, h, streamResets, fillColor);
    }

    public static final class BuilderImpl implements RfbRectangleTightFill.Builder {
        private int x, y, width, height, streamResets;
        private byte[] fillColor;

        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }
        @Override public Builder streamResets(int v) { this.streamResets = v; return this; }
        @Override public Builder fillColor(byte[] v) { this.fillColor = v; return this; }

        @Override public RfbRectangleTightFill build() {
            return new RfbRectangleTightFillImpl(x, y, width, height, streamResets, fillColor);
        }
    }
}
