package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleTightBasic;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public final class RfbRectangleTightBasicImpl implements RfbRectangleTightBasic {
    private final int x, y, width, height;
    private final int streamResets;
    private final int streamNumber;
    private final int filterType;
    private final int paletteSize;
    private final byte[] palette;
    private final byte[] compressedData;

    public RfbRectangleTightBasicImpl(int x, int y, int width, int height,
            int streamResets, int streamNumber, int filterType,
            int paletteSize, byte[] palette, byte[] compressedData) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.streamResets = streamResets; this.streamNumber = streamNumber;
        this.filterType = filterType; this.paletteSize = paletteSize;
        this.palette = palette; this.compressedData = compressedData;
    }

    @Override public int x() { return x; }
    @Override public int y() { return y; }
    @Override public int width() { return width; }
    @Override public int height() { return height; }
    @Override public int streamResets() { return streamResets; }
    @Override public int streamNumber() { return streamNumber; }
    @Override public int filterType() { return filterType; }
    @Override public int paletteSize() { return paletteSize; }
    @Override public byte[] palette() { return palette; }
    @Override public byte[] compressedData() { return compressedData; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RfbRectangleTightBasic r)) return false;
        return x == r.x() && y == r.y() && width == r.width() && height == r.height()
                && streamResets == r.streamResets() && streamNumber == r.streamNumber()
                && filterType == r.filterType() && paletteSize == r.paletteSize()
                && Arrays.equals(palette, r.palette())
                && Arrays.equals(compressedData, r.compressedData());
    }

    @Override public int hashCode() {
        return Objects.hash(x, y, width, height, streamResets, streamNumber, filterType, paletteSize,
                Arrays.hashCode(palette), Arrays.hashCode(compressedData));
    }
    @Override public String toString() {
        return "RfbRectangleTightBasic[x=" + x + ", y=" + y + ", width=" + width
                + ", height=" + height + ", filterType=" + filterType + "]";
    }

    @Override
    public void write(java.io.OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeShort(x); dos.writeShort(y); dos.writeShort(width); dos.writeShort(height);
        dos.writeInt(ENCODING_TYPE);
        // BasicCompression: bit7=0, bit6=ReadFilter, bits5-4=streamNumber, bits3-0=streamResets
        boolean writeFilter = (filterType != FILTER_COPY);
        int ctrl = (streamResets & 0xF)
                | ((streamNumber & 0x3) << 4)
                | (writeFilter ? 0x40 : 0);
        dos.writeByte(ctrl);
        if (writeFilter) {
            dos.writeByte(filterType);
        }
        if (filterType == FILTER_PALETTE) {
            dos.writeByte(paletteSize);
            if (palette != null) dos.write(palette);
        }
        byte[] data = compressedData != null ? compressedData : new byte[0];
        TightIo.writeCompactLength(dos, data.length);
        dos.write(data);
    }

    public static RfbRectangleTightBasic readPayload(DataInputStream dis, int x, int y, int w, int h,
            PixelFormat pf, int compressionControlByte) throws IOException {
        int streamResets = compressionControlByte & 0xF;
        int highNibble = (compressionControlByte >> 4) & 0xF;
        boolean readFilter = (highNibble & 0x4) != 0;
        int streamNumber = highNibble & 0x3;

        int filterType = FILTER_COPY;
        int paletteSize = 0;
        byte[] palette = null;

        if (readFilter) {
            filterType = dis.readUnsignedByte();
        }
        if (filterType == FILTER_PALETTE) {
            paletteSize = dis.readUnsignedByte(); // wire value is n-1 (number of colours minus 1)
            int tpixelSize = TightIo.tpixelSize(pf);
            palette = new byte[(paletteSize + 1) * tpixelSize]; // actual count is paletteSize+1
            dis.readFully(palette);
        }
        int len = TightIo.readCompactLength(dis);
        byte[] compressedData = new byte[len];
        dis.readFully(compressedData);
        return new RfbRectangleTightBasicImpl(x, y, w, h, streamResets, streamNumber,
                filterType, paletteSize, palette, compressedData);
    }

    public static final class BuilderImpl implements RfbRectangleTightBasic.Builder {
        private int x, y, width, height, streamResets, streamNumber, filterType, paletteSize;
        private byte[] palette, compressedData;

        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }
        @Override public Builder streamResets(int v) { this.streamResets = v; return this; }
        @Override public Builder streamNumber(int v) { this.streamNumber = v; return this; }
        @Override public Builder filterType(int v) { this.filterType = v; return this; }
        @Override public Builder paletteSize(int v) { this.paletteSize = v; return this; }
        @Override public Builder palette(byte[] v) { this.palette = v; return this; }
        @Override public Builder compressedData(byte[] v) { this.compressedData = v; return this; }

        @Override public RfbRectangleTightBasic build() {
            return new RfbRectangleTightBasicImpl(x, y, width, height, streamResets, streamNumber,
                    filterType, paletteSize, palette, compressedData);
        }
    }
}
