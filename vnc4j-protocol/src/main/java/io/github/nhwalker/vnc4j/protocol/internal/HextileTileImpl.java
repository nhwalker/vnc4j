package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.messages.HextileSubrect;
import io.github.nhwalker.vnc4j.protocol.messages.HextileTile;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class HextileTileImpl implements HextileTile {
    private final int subencoding;
    private final byte[] rawPixels;
    private final byte[] background;
    private final byte[] foreground;
    private final List<HextileSubrect> subrects;

    public HextileTileImpl(int subencoding, byte[] rawPixels, byte[] background,
            byte[] foreground, List<HextileSubrect> subrects) {
        this.subencoding = subencoding;
        this.rawPixels = rawPixels;
        this.background = background;
        this.foreground = foreground;
        this.subrects = subrects != null ? subrects : List.of();
    }

    @Override public int subencoding() { return subencoding; }
    @Override public byte[] rawPixels() { return rawPixels; }
    @Override public byte[] background() { return background; }
    @Override public byte[] foreground() { return foreground; }
    @Override public List<HextileSubrect> subrects() { return subrects; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HextileTile other)) return false;
        return subencoding == other.subencoding()
                && Arrays.equals(rawPixels, other.rawPixels())
                && Arrays.equals(background, other.background())
                && Arrays.equals(foreground, other.foreground())
                && Objects.equals(subrects, other.subrects());
    }

    @Override
    public int hashCode() {
        return Objects.hash(subencoding, Arrays.hashCode(rawPixels),
                Arrays.hashCode(background), Arrays.hashCode(foreground), subrects);
    }

    @Override
    public String toString() {
        return "HextileTile[subencoding=" + subencoding + ", rawPixels=" + Arrays.toString(rawPixels)
                + ", background=" + Arrays.toString(background)
                + ", foreground=" + Arrays.toString(foreground)
                + ", subrects=" + subrects + "]";
    }

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(subencoding);
        if ((subencoding & SUBENC_RAW) != 0) {
            if (rawPixels != null) {
                dos.write(rawPixels);
            }
        } else {
            if ((subencoding & SUBENC_BACKGROUND_SPECIFIED) != 0 && background != null) {
                dos.write(background);
            }
            if ((subencoding & SUBENC_FOREGROUND_SPECIFIED) != 0 && foreground != null) {
                dos.write(foreground);
            }
            if ((subencoding & SUBENC_ANY_SUBRECTS) != 0) {
                List<HextileSubrect> rects = subrects != null ? subrects : List.of();
                dos.writeByte(rects.size());
                boolean coloured = (subencoding & SUBENC_SUBRECTS_COLOURED) != 0;
                for (HextileSubrect sr : rects) {
                    sr.write(out, coloured);
                }
            }
        }
    }

    public static HextileTile read(InputStream in, int tileWidth, int tileHeight, int bytesPerPixel)
            throws IOException {
        DataInputStream dis = new DataInputStream(in);
        int subencoding = dis.readUnsignedByte();
        byte[] rawPixels = null;
        byte[] background = null;
        byte[] foreground = null;
        List<HextileSubrect> subrects = List.of();

        if ((subencoding & SUBENC_RAW) != 0) {
            rawPixels = new byte[tileWidth * tileHeight * bytesPerPixel];
            dis.readFully(rawPixels);
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
                int count = dis.readUnsignedByte();
                boolean coloured = (subencoding & SUBENC_SUBRECTS_COLOURED) != 0;
                List<HextileSubrect> list = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    list.add(HextileSubrect.read(in, coloured, bytesPerPixel));
                }
                subrects = list;
            }
        }
        return new HextileTileImpl(subencoding, rawPixels, background, foreground, subrects);
    }

    public static final class BuilderImpl implements HextileTile.Builder {
        private int subencoding;
        private byte[] rawPixels;
        private byte[] background;
        private byte[] foreground;
        private List<HextileSubrect> subrects;

        @Override public Builder subencoding(int v) { this.subencoding = v; return this; }
        @Override public Builder rawPixels(byte[] v) { this.rawPixels = v; return this; }
        @Override public Builder background(byte[] v) { this.background = v; return this; }
        @Override public Builder foreground(byte[] v) { this.foreground = v; return this; }
        @Override public Builder subrects(List<HextileSubrect> v) { this.subrects = v; return this; }

        @Override
        public HextileTile build() {
            return new HextileTileImpl(subencoding, rawPixels, background, foreground, subrects);
        }
    }
}
