package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.HextileSubrect;
import io.github.nhwalker.vnc4j.protocol.ZlibHexTile;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class ZlibHexTileImpl implements ZlibHexTile {
    private final int subencoding;
    private final byte[] zlibRawData;
    private final byte[] background;
    private final byte[] foreground;
    private final byte[] zlibSubrectData;
    private final List<HextileSubrect> subrects;

    public ZlibHexTileImpl(int subencoding, byte[] zlibRawData, byte[] background,
            byte[] foreground, byte[] zlibSubrectData, List<HextileSubrect> subrects) {
        this.subencoding = subencoding;
        this.zlibRawData = zlibRawData;
        this.background = background;
        this.foreground = foreground;
        this.zlibSubrectData = zlibSubrectData;
        this.subrects = subrects != null ? subrects : List.of();
    }

    @Override public int subencoding() { return subencoding; }
    @Override public byte[] zlibRawData() { return zlibRawData; }
    @Override public byte[] background() { return background; }
    @Override public byte[] foreground() { return foreground; }
    @Override public byte[] zlibSubrectData() { return zlibSubrectData; }
    @Override public List<HextileSubrect> subrects() { return subrects; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ZlibHexTile other)) return false;
        return subencoding == other.subencoding()
                && Arrays.equals(zlibRawData, other.zlibRawData())
                && Arrays.equals(background, other.background())
                && Arrays.equals(foreground, other.foreground())
                && Arrays.equals(zlibSubrectData, other.zlibSubrectData())
                && Objects.equals(subrects, other.subrects());
    }

    @Override
    public int hashCode() {
        return Objects.hash(subencoding, Arrays.hashCode(zlibRawData),
                Arrays.hashCode(background), Arrays.hashCode(foreground),
                Arrays.hashCode(zlibSubrectData), subrects);
    }

    @Override
    public String toString() {
        return "ZlibHexTile[subencoding=" + subencoding
                + ", zlibRawData.len=" + (zlibRawData != null ? zlibRawData.length : "null")
                + ", background=" + Arrays.toString(background)
                + ", foreground=" + Arrays.toString(foreground)
                + ", zlibSubrectData.len=" + (zlibSubrectData != null ? zlibSubrectData.length : "null")
                + ", subrects=" + subrects + "]";
    }

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(subencoding);
        if ((subencoding & SUBENC_ZLIB_RAW) != 0) {
            byte[] data = zlibRawData != null ? zlibRawData : new byte[0];
            dos.writeShort(data.length);
            dos.write(data);
        } else {
            if ((subencoding & SUBENC_BACKGROUND_SPECIFIED) != 0 && background != null) {
                dos.write(background);
            }
            if ((subencoding & SUBENC_FOREGROUND_SPECIFIED) != 0 && foreground != null) {
                dos.write(foreground);
            }
            if ((subencoding & SUBENC_ANY_SUBRECTS) != 0) {
                if ((subencoding & SUBENC_ZLIB) != 0) {
                    byte[] data = zlibSubrectData != null ? zlibSubrectData : new byte[0];
                    dos.writeShort(data.length);
                    dos.write(data);
                } else {
                    List<HextileSubrect> rects = subrects != null ? subrects : List.of();
                    dos.writeByte(rects.size());
                    boolean coloured = (subencoding & SUBENC_SUBRECTS_COLOURED) != 0;
                    for (HextileSubrect sr : rects) {
                        sr.write(out, coloured);
                    }
                }
            }
        }
    }

    public static ZlibHexTile read(InputStream in, int tileWidth, int tileHeight, int bytesPerPixel)
            throws IOException {
        DataInputStream dis = new DataInputStream(in);
        int subencoding = dis.readUnsignedByte();
        byte[] zlibRawData = null;
        byte[] background = null;
        byte[] foreground = null;
        byte[] zlibSubrectData = null;
        List<HextileSubrect> subrects = List.of();

        if ((subencoding & SUBENC_ZLIB_RAW) != 0) {
            int len = dis.readUnsignedShort();
            zlibRawData = new byte[len];
            dis.readFully(zlibRawData);
        } else {
            if ((subencoding & SUBENC_BACKGROUND_SPECIFIED) != 0) {
                background = new byte[bytesPerPixel];
                dis.readFully(background);
            }
            if ((subencoding & SUBENC_FOREGROUND_SPECIFIED) != 0) {
                foreground = new byte[bytesPerPixel];
                dis.readFully(foreground);
            }
            if ((subencoding & SUBENC_ANY_SUBRECTS) != 0) {
                if ((subencoding & SUBENC_ZLIB) != 0) {
                    int len = dis.readUnsignedShort();
                    zlibSubrectData = new byte[len];
                    dis.readFully(zlibSubrectData);
                } else {
                    int count = dis.readUnsignedByte();
                    boolean coloured = (subencoding & SUBENC_SUBRECTS_COLOURED) != 0;
                    List<HextileSubrect> list = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        list.add(HextileSubrect.read(in, coloured, bytesPerPixel));
                    }
                    subrects = list;
                }
            }
        }
        return new ZlibHexTileImpl(subencoding, zlibRawData, background, foreground, zlibSubrectData, subrects);
    }

    public static final class BuilderImpl implements ZlibHexTile.Builder {
        private int subencoding;
        private byte[] zlibRawData;
        private byte[] background;
        private byte[] foreground;
        private byte[] zlibSubrectData;
        private List<HextileSubrect> subrects;

        @Override public Builder subencoding(int v) { this.subencoding = v; return this; }
        @Override public Builder zlibRawData(byte[] v) { this.zlibRawData = v; return this; }
        @Override public Builder background(byte[] v) { this.background = v; return this; }
        @Override public Builder foreground(byte[] v) { this.foreground = v; return this; }
        @Override public Builder zlibSubrectData(byte[] v) { this.zlibSubrectData = v; return this; }
        @Override public Builder subrects(List<HextileSubrect> v) { this.subrects = v; return this; }

        @Override
        public ZlibHexTile build() {
            return new ZlibHexTileImpl(subencoding, zlibRawData, background, foreground, zlibSubrectData, subrects);
        }
    }
}
