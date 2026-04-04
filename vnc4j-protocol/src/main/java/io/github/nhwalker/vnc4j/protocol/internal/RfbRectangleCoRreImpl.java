package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.CoRreSubrect;
import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleCoRre;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class RfbRectangleCoRreImpl implements RfbRectangleCoRre {
    private final int x, y, width, height;
    private final byte[] background;
    private final List<CoRreSubrect> subrects;

    public RfbRectangleCoRreImpl(int x, int y, int width, int height, byte[] background, List<CoRreSubrect> subrects) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.background = background;
        this.subrects = subrects != null ? subrects : List.of();
    }

    @Override public int x() { return x; }
    @Override public int y() { return y; }
    @Override public int width() { return width; }
    @Override public int height() { return height; }
    @Override public byte[] background() { return background; }
    @Override public List<CoRreSubrect> subrects() { return subrects; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RfbRectangleCoRre r)) return false;
        return x == r.x() && y == r.y() && width == r.width() && height == r.height()
                && Arrays.equals(background, r.background()) && Objects.equals(subrects, r.subrects());
    }

    @Override public int hashCode() {
        return Objects.hash(x, y, width, height, Arrays.hashCode(background), subrects);
    }
    @Override public String toString() {
        return "RfbRectangleCoRre[x=" + x + ", y=" + y + ", width=" + width + ", height=" + height
                + ", subrects.size=" + subrects.size() + "]";
    }

    @Override
    public void write(java.io.OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeShort(x); dos.writeShort(y); dos.writeShort(width); dos.writeShort(height);
        dos.writeInt(ENCODING_TYPE);
        List<CoRreSubrect> rects = subrects != null ? subrects : List.of();
        dos.writeInt(rects.size());
        if (background != null) dos.write(background);
        for (CoRreSubrect sr : rects) {
            sr.write(out);
        }
    }

    public static RfbRectangleCoRre readPayload(DataInputStream dis, int x, int y, int w, int h,
            PixelFormat pf) throws IOException {
        int bpp = pf.bitsPerPixel() / 8;
        int count = dis.readInt();
        byte[] background = new byte[bpp];
        dis.readFully(background);
        List<CoRreSubrect> subrects = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            subrects.add(CoRreSubrect.read(dis, bpp));
        }
        return new RfbRectangleCoRreImpl(x, y, w, h, background, subrects);
    }

    public static final class BuilderImpl implements RfbRectangleCoRre.Builder {
        private int x, y, width, height;
        private byte[] background;
        private List<CoRreSubrect> subrects;

        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }
        @Override public Builder background(byte[] v) { this.background = v; return this; }
        @Override public Builder subrects(List<CoRreSubrect> v) { this.subrects = v; return this; }

        @Override public RfbRectangleCoRre build() {
            return new RfbRectangleCoRreImpl(x, y, width, height, background, subrects);
        }
    }
}
