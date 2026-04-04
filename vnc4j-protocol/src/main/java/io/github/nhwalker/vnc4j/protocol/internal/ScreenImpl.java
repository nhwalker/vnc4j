package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.Screen;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public record ScreenImpl(long id, int x, int y, int width, int height, long flags) implements Screen {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeInt((int)(id & 0xFFFFFFFFL));
        dos.writeShort(x);
        dos.writeShort(y);
        dos.writeShort(width);
        dos.writeShort(height);
        dos.writeInt((int)(flags & 0xFFFFFFFFL));
    }

    public static Screen read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        long id = Integer.toUnsignedLong(dis.readInt());
        int x = dis.readShort();
        int y = dis.readShort();
        int width = dis.readUnsignedShort();
        int height = dis.readUnsignedShort();
        long flags = Integer.toUnsignedLong(dis.readInt());
        return new ScreenImpl(id, x, y, width, height, flags);
    }

    public static final class BuilderImpl implements Screen.Builder {
        private long id;
        private int x;
        private int y;
        private int width;
        private int height;
        private long flags;

        @Override public Builder id(long v) { this.id = v; return this; }
        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }
        @Override public Builder flags(long v) { this.flags = v; return this; }

        @Override
        public Screen build() {
            return new ScreenImpl(id, x, y, width, height, flags);
        }
    }
}
