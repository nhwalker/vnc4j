package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleXCursor;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public final class RfbRectangleXCursorImpl implements RfbRectangleXCursor {
    private final int x, y, width, height;
    private final int primaryR, primaryG, primaryB;
    private final int secondaryR, secondaryG, secondaryB;
    private final byte[] bitmap;
    private final byte[] bitmask;

    public RfbRectangleXCursorImpl(int x, int y, int width, int height,
            int primaryR, int primaryG, int primaryB,
            int secondaryR, int secondaryG, int secondaryB,
            byte[] bitmap, byte[] bitmask) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.primaryR = primaryR; this.primaryG = primaryG; this.primaryB = primaryB;
        this.secondaryR = secondaryR; this.secondaryG = secondaryG; this.secondaryB = secondaryB;
        this.bitmap = bitmap; this.bitmask = bitmask;
    }

    @Override public int x() { return x; }
    @Override public int y() { return y; }
    @Override public int width() { return width; }
    @Override public int height() { return height; }
    @Override public int primaryR() { return primaryR; }
    @Override public int primaryG() { return primaryG; }
    @Override public int primaryB() { return primaryB; }
    @Override public int secondaryR() { return secondaryR; }
    @Override public int secondaryG() { return secondaryG; }
    @Override public int secondaryB() { return secondaryB; }
    @Override public byte[] bitmap() { return bitmap; }
    @Override public byte[] bitmask() { return bitmask; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RfbRectangleXCursor r)) return false;
        return x == r.x() && y == r.y() && width == r.width() && height == r.height()
                && primaryR == r.primaryR() && primaryG == r.primaryG() && primaryB == r.primaryB()
                && secondaryR == r.secondaryR() && secondaryG == r.secondaryG() && secondaryB == r.secondaryB()
                && Arrays.equals(bitmap, r.bitmap()) && Arrays.equals(bitmask, r.bitmask());
    }

    @Override public int hashCode() {
        return Objects.hash(x, y, width, height, primaryR, primaryG, primaryB,
                secondaryR, secondaryG, secondaryB, Arrays.hashCode(bitmap), Arrays.hashCode(bitmask));
    }
    @Override public String toString() {
        return "RfbRectangleXCursor[x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
    }

    @Override
    public void write(java.io.OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeShort(x); dos.writeShort(y); dos.writeShort(width); dos.writeShort(height);
        dos.writeInt(ENCODING_TYPE);
        if (width > 0 && height > 0) {
            dos.writeByte(primaryR); dos.writeByte(primaryG); dos.writeByte(primaryB);
            dos.writeByte(secondaryR); dos.writeByte(secondaryG); dos.writeByte(secondaryB);
            if (bitmap != null) dos.write(bitmap);
            if (bitmask != null) dos.write(bitmask);
        }
    }

    public static RfbRectangleXCursor readPayload(DataInputStream dis, int x, int y, int w, int h,
            PixelFormat pf) throws IOException {
        int primaryR = 0, primaryG = 0, primaryB = 0;
        int secondaryR = 0, secondaryG = 0, secondaryB = 0;
        byte[] bitmap = null, bitmask = null;
        if (w > 0 && h > 0) {
            primaryR = dis.readUnsignedByte();
            primaryG = dis.readUnsignedByte();
            primaryB = dis.readUnsignedByte();
            secondaryR = dis.readUnsignedByte();
            secondaryG = dis.readUnsignedByte();
            secondaryB = dis.readUnsignedByte();
            int maskRowBytes = (w + 7) / 8;
            bitmap = new byte[maskRowBytes * h];
            dis.readFully(bitmap);
            bitmask = new byte[maskRowBytes * h];
            dis.readFully(bitmask);
        }
        return new RfbRectangleXCursorImpl(x, y, w, h,
                primaryR, primaryG, primaryB, secondaryR, secondaryG, secondaryB, bitmap, bitmask);
    }

    public static final class BuilderImpl implements RfbRectangleXCursor.Builder {
        private int x, y, width, height;
        private int primaryR, primaryG, primaryB;
        private int secondaryR, secondaryG, secondaryB;
        private byte[] bitmap, bitmask;

        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }
        @Override public Builder primaryR(int v) { this.primaryR = v; return this; }
        @Override public Builder primaryG(int v) { this.primaryG = v; return this; }
        @Override public Builder primaryB(int v) { this.primaryB = v; return this; }
        @Override public Builder secondaryR(int v) { this.secondaryR = v; return this; }
        @Override public Builder secondaryG(int v) { this.secondaryG = v; return this; }
        @Override public Builder secondaryB(int v) { this.secondaryB = v; return this; }
        @Override public Builder bitmap(byte[] v) { this.bitmap = v; return this; }
        @Override public Builder bitmask(byte[] v) { this.bitmask = v; return this; }

        @Override public RfbRectangleXCursor build() {
            return new RfbRectangleXCursorImpl(x, y, width, height,
                    primaryR, primaryG, primaryB, secondaryR, secondaryG, secondaryB, bitmap, bitmask);
        }
    }
}
