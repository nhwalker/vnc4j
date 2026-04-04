package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.Screen;
import io.github.nhwalker.vnc4j.protocol.SetDesktopSize;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public record SetDesktopSizeImpl(int width, int height, List<Screen> screens) implements SetDesktopSize {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        List<Screen> sc = screens != null ? screens : List.of();
        dos.writeByte(251); // message-type
        dos.writeByte(0); // padding
        dos.writeShort(width);
        dos.writeShort(height);
        dos.writeByte(sc.size());
        dos.writeByte(0); // padding
        for (Screen s : sc) {
            s.write(out);
        }
    }

    public static SetDesktopSize read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        dis.readUnsignedByte(); // padding
        int width = dis.readUnsignedShort();
        int height = dis.readUnsignedShort();
        int numScreens = dis.readUnsignedByte();
        dis.readUnsignedByte(); // padding
        List<Screen> screens = new ArrayList<>(numScreens);
        for (int i = 0; i < numScreens; i++) {
            screens.add(Screen.read(in));
        }
        return new SetDesktopSizeImpl(width, height, screens);
    }

    public static final class BuilderImpl implements SetDesktopSize.Builder {
        private int width;
        private int height;
        private List<Screen> screens;

        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }
        @Override public Builder screens(List<Screen> v) { this.screens = v; return this; }

        @Override
        public SetDesktopSize build() {
            return new SetDesktopSizeImpl(width, height, screens);
        }
    }
}
