package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.messages.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.messages.RfbRectangleExtendedDesktopSize;
import io.github.nhwalker.vnc4j.protocol.messages.Screen;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RfbRectangleExtendedDesktopSizeImpl implements RfbRectangleExtendedDesktopSize {
    private final int x, y, width, height;
    private final List<Screen> screens;

    public RfbRectangleExtendedDesktopSizeImpl(int x, int y, int width, int height, List<Screen> screens) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.screens = screens != null ? screens : List.of();
    }

    @Override public int x() { return x; }
    @Override public int y() { return y; }
    @Override public int width() { return width; }
    @Override public int height() { return height; }
    @Override public List<Screen> screens() { return screens; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RfbRectangleExtendedDesktopSize r)) return false;
        return x == r.x() && y == r.y() && width == r.width() && height == r.height()
                && Objects.equals(screens, r.screens());
    }

    @Override public int hashCode() { return Objects.hash(x, y, width, height, screens); }
    @Override public String toString() {
        return "RfbRectangleExtendedDesktopSize[x=" + x + ", y=" + y + ", width=" + width
                + ", height=" + height + ", screens=" + screens + "]";
    }

    @Override
    public void write(java.io.OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeShort(x); dos.writeShort(y); dos.writeShort(width); dos.writeShort(height);
        dos.writeInt(ENCODING_TYPE);
        List<Screen> s = screens != null ? screens : List.of();
        dos.writeByte(s.size());
        dos.writeByte(0); dos.writeByte(0); dos.writeByte(0); // 3 bytes padding
        for (Screen screen : s) {
            screen.write(out);
        }
    }

    public static RfbRectangleExtendedDesktopSize readPayload(DataInputStream dis, int x, int y, int w, int h,
            PixelFormat pf) throws IOException {
        int numScreens = dis.readUnsignedByte();
        dis.skipBytes(3); // padding
        List<Screen> screens = new ArrayList<>(numScreens);
        for (int i = 0; i < numScreens; i++) {
            screens.add(Screen.read(dis));
        }
        return new RfbRectangleExtendedDesktopSizeImpl(x, y, w, h, screens);
    }

    public static final class BuilderImpl implements RfbRectangleExtendedDesktopSize.Builder {
        private int x, y, width, height;
        private List<Screen> screens;

        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }
        @Override public Builder screens(List<Screen> v) { this.screens = v; return this; }

        @Override public RfbRectangleExtendedDesktopSize build() {
            return new RfbRectangleExtendedDesktopSizeImpl(x, y, width, height, screens);
        }
    }
}
