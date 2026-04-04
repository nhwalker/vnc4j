package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleTightPngJpeg;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public final class RfbRectangleTightPngJpegImpl implements RfbRectangleTightPngJpeg {
    private final int x, y, width, height;
    private final int streamResets;
    private final byte[] jpegData;

    public RfbRectangleTightPngJpegImpl(int x, int y, int width, int height,
            int streamResets, byte[] jpegData) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.streamResets = streamResets; this.jpegData = jpegData;
    }

    @Override public int x() { return x; }
    @Override public int y() { return y; }
    @Override public int width() { return width; }
    @Override public int height() { return height; }
    @Override public int streamResets() { return streamResets; }
    @Override public byte[] jpegData() { return jpegData; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RfbRectangleTightPngJpeg r)) return false;
        return x == r.x() && y == r.y() && width == r.width() && height == r.height()
                && streamResets == r.streamResets() && Arrays.equals(jpegData, r.jpegData());
    }

    @Override public int hashCode() {
        return Objects.hash(x, y, width, height, streamResets, Arrays.hashCode(jpegData));
    }
    @Override public String toString() {
        return "RfbRectangleTightPngJpeg[x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
    }

    @Override
    public void write(java.io.OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeShort(x); dos.writeShort(y); dos.writeShort(width); dos.writeShort(height);
        dos.writeInt(ENCODING_TYPE);
        dos.writeByte((streamResets & 0xF) | 0x90);
        byte[] data = jpegData != null ? jpegData : new byte[0];
        TightIo.writeCompactLength(dos, data.length);
        dos.write(data);
    }

    public static RfbRectangleTightPngJpeg readPayload(DataInputStream dis, int x, int y, int w, int h,
            PixelFormat pf, int streamResets) throws IOException {
        int len = TightIo.readCompactLength(dis);
        byte[] data = new byte[len];
        dis.readFully(data);
        return new RfbRectangleTightPngJpegImpl(x, y, w, h, streamResets, data);
    }

    public static final class BuilderImpl implements RfbRectangleTightPngJpeg.Builder {
        private int x, y, width, height, streamResets;
        private byte[] jpegData;

        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }
        @Override public Builder streamResets(int v) { this.streamResets = v; return this; }
        @Override public Builder jpegData(byte[] v) { this.jpegData = v; return this; }

        @Override public RfbRectangleTightPngJpeg build() {
            return new RfbRectangleTightPngJpegImpl(x, y, width, height, streamResets, jpegData);
        }
    }
}
