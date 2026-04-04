package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public record PixelFormatImpl(
        int bitsPerPixel,
        int depth,
        boolean bigEndian,
        boolean trueColour,
        int redMax,
        int greenMax,
        int blueMax,
        int redShift,
        int greenShift,
        int blueShift
) implements PixelFormat {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(bitsPerPixel);
        dos.writeByte(depth);
        dos.writeByte(bigEndian ? 1 : 0);
        dos.writeByte(trueColour ? 1 : 0);
        dos.writeShort(redMax);
        dos.writeShort(greenMax);
        dos.writeShort(blueMax);
        dos.writeByte(redShift);
        dos.writeByte(greenShift);
        dos.writeByte(blueShift);
        dos.writeByte(0); // padding
        dos.writeByte(0);
        dos.writeByte(0);
    }

    public static PixelFormat read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        int bitsPerPixel = dis.readUnsignedByte();
        int depth = dis.readUnsignedByte();
        boolean bigEndian = dis.readUnsignedByte() != 0;
        boolean trueColour = dis.readUnsignedByte() != 0;
        int redMax = dis.readUnsignedShort();
        int greenMax = dis.readUnsignedShort();
        int blueMax = dis.readUnsignedShort();
        int redShift = dis.readUnsignedByte();
        int greenShift = dis.readUnsignedByte();
        int blueShift = dis.readUnsignedByte();
        dis.skipBytes(3); // padding
        return new PixelFormatImpl(bitsPerPixel, depth, bigEndian, trueColour,
                redMax, greenMax, blueMax, redShift, greenShift, blueShift);
    }

    public static final class BuilderImpl implements PixelFormat.Builder {
        private int bitsPerPixel;
        private int depth;
        private boolean bigEndian;
        private boolean trueColour;
        private int redMax;
        private int greenMax;
        private int blueMax;
        private int redShift;
        private int greenShift;
        private int blueShift;

        @Override public Builder bitsPerPixel(int v) { this.bitsPerPixel = v; return this; }
        @Override public Builder depth(int v) { this.depth = v; return this; }
        @Override public Builder bigEndian(boolean v) { this.bigEndian = v; return this; }
        @Override public Builder trueColour(boolean v) { this.trueColour = v; return this; }
        @Override public Builder redMax(int v) { this.redMax = v; return this; }
        @Override public Builder greenMax(int v) { this.greenMax = v; return this; }
        @Override public Builder blueMax(int v) { this.blueMax = v; return this; }
        @Override public Builder redShift(int v) { this.redShift = v; return this; }
        @Override public Builder greenShift(int v) { this.greenShift = v; return this; }
        @Override public Builder blueShift(int v) { this.blueShift = v; return this; }

        @Override
        public PixelFormat build() {
            return new PixelFormatImpl(bitsPerPixel, depth, bigEndian, trueColour,
                    redMax, greenMax, blueMax, redShift, greenShift, blueShift);
        }
    }
}
