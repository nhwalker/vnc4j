package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.RfbRectangle;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

public final class RfbRectangleImpl implements RfbRectangle {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int encodingType;
    private final byte[] data;

    public RfbRectangleImpl(int x, int y, int width, int height, int encodingType, byte[] data) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.encodingType = encodingType;
        this.data = data;
    }

    @Override public int x() { return x; }
    @Override public int y() { return y; }
    @Override public int width() { return width; }
    @Override public int height() { return height; }
    @Override public int encodingType() { return encodingType; }
    @Override public byte[] data() { return data; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RfbRectangle other)) return false;
        return x == other.x() && y == other.y() && width == other.width()
                && height == other.height() && encodingType == other.encodingType()
                && Arrays.equals(data, other.data());
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height, encodingType, Arrays.hashCode(data));
    }

    @Override
    public String toString() {
        return "RfbRectangle[x=" + x + ", y=" + y + ", width=" + width + ", height=" + height
                + ", encodingType=" + encodingType + ", data=" + Arrays.toString(data) + "]";
    }

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeShort(x);
        dos.writeShort(y);
        dos.writeShort(width);
        dos.writeShort(height);
        dos.writeInt(encodingType);
        if (data != null) {
            dos.write(data);
        }
    }

    public static RfbRectangle read(InputStream in, int dataLength) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        int x = dis.readUnsignedShort();
        int y = dis.readUnsignedShort();
        int width = dis.readUnsignedShort();
        int height = dis.readUnsignedShort();
        int encodingType = dis.readInt();
        byte[] data = new byte[dataLength];
        if (dataLength > 0) {
            dis.readFully(data);
        }
        return new RfbRectangleImpl(x, y, width, height, encodingType, data);
    }

    public static final class BuilderImpl implements RfbRectangle.Builder {
        private int x;
        private int y;
        private int width;
        private int height;
        private int encodingType;
        private byte[] data;

        @Override public Builder x(int x) { this.x = x; return this; }
        @Override public Builder y(int y) { this.y = y; return this; }
        @Override public Builder width(int width) { this.width = width; return this; }
        @Override public Builder height(int height) { this.height = height; return this; }
        @Override public Builder encodingType(int encodingType) { this.encodingType = encodingType; return this; }
        @Override public Builder data(byte[] data) { this.data = data; return this; }

        @Override
        public RfbRectangle build() {
            return new RfbRectangleImpl(x, y, width, height, encodingType, data);
        }
    }
}
