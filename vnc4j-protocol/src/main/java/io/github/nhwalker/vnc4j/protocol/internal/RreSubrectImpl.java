package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.messages.RreSubrect;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

public final class RreSubrectImpl implements RreSubrect {
    private final byte[] pixel;
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public RreSubrectImpl(byte[] pixel, int x, int y, int width, int height) {
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
        if (!(o instanceof RreSubrect other)) return false;
        return x == other.x() && y == other.y() && width == other.width()
                && height == other.height() && Arrays.equals(pixel, other.pixel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(pixel), x, y, width, height);
    }

    @Override
    public String toString() {
        return "RreSubrect[pixel=" + Arrays.toString(pixel) + ", x=" + x + ", y=" + y
                + ", width=" + width + ", height=" + height + "]";
    }

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.write(pixel);
        dos.writeShort(x);
        dos.writeShort(y);
        dos.writeShort(width);
        dos.writeShort(height);
    }

    public static RreSubrect read(InputStream in, int bytesPerPixel) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        byte[] pixel = new byte[bytesPerPixel];
        dis.readFully(pixel);
        int x = dis.readUnsignedShort();
        int y = dis.readUnsignedShort();
        int width = dis.readUnsignedShort();
        int height = dis.readUnsignedShort();
        return new RreSubrectImpl(pixel, x, y, width, height);
    }

    public static final class BuilderImpl implements RreSubrect.Builder {
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
        public RreSubrect build() {
            return new RreSubrectImpl(pixel, x, y, width, height);
        }
    }
}
