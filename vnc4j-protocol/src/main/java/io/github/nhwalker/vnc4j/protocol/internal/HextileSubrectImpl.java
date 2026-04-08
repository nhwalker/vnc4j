package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.messages.HextileSubrect;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

public final class HextileSubrectImpl implements HextileSubrect {
    private final byte[] pixel;
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public HextileSubrectImpl(byte[] pixel, int x, int y, int width, int height) {
        this.pixel = pixel;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override public byte[] pixel() { return pixel; }
    @Override public int x() { return x; }
    @Override public int y() { return y; }
    @Override public int width() { return width; }
    @Override public int height() { return height; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HextileSubrect other)) return false;
        return x == other.x() && y == other.y() && width == other.width()
                && height == other.height() && Arrays.equals(pixel, other.pixel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(pixel), x, y, width, height);
    }

    @Override
    public String toString() {
        return "HextileSubrect[pixel=" + Arrays.toString(pixel) + ", x=" + x + ", y=" + y
                + ", width=" + width + ", height=" + height + "]";
    }

    @Override
    public void write(OutputStream out, boolean subrectsColoured) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        if (subrectsColoured && pixel != null) {
            dos.write(pixel);
        }
        dos.writeByte((x << 4) | (y & 0xF));
        dos.writeByte(((width - 1) << 4) | ((height - 1) & 0xF));
    }

    public static HextileSubrect read(InputStream in, boolean subrectsColoured, int bytesPerPixel)
            throws IOException {
        DataInputStream dis = new DataInputStream(in);
        byte[] pixel = null;
        if (subrectsColoured) {
            pixel = new byte[bytesPerPixel];
            dis.readFully(pixel);
        }
        int xy = dis.readUnsignedByte();
        int wh = dis.readUnsignedByte();
        int x = (xy >> 4) & 0xF;
        int y = xy & 0xF;
        int w = ((wh >> 4) & 0xF) + 1;
        int h = (wh & 0xF) + 1;
        return new HextileSubrectImpl(pixel, x, y, w, h);
    }

    public static final class BuilderImpl implements HextileSubrect.Builder {
        private byte[] pixel;
        private int x;
        private int y;
        private int width;
        private int height;

        @Override public Builder pixel(byte[] v) { this.pixel = v; return this; }
        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }

        @Override
        public HextileSubrect build() {
            return new HextileSubrectImpl(pixel, x, y, width, height);
        }
    }
}
