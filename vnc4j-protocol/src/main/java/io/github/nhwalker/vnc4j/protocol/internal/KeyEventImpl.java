package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.messages.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public record KeyEventImpl(boolean down, int key) implements KeyEvent {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(4); // message-type
        dos.writeByte(down ? 1 : 0);
        dos.writeShort(0); // padding
        dos.writeInt(key);
    }

    public static KeyEvent read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        boolean down = dis.readUnsignedByte() != 0;
        dis.readShort(); // padding
        int key = dis.readInt();
        return new KeyEventImpl(down, key);
    }

    public static final class BuilderImpl implements KeyEvent.Builder {
        private boolean down;
        private int key;

        @Override public Builder down(boolean v) { this.down = v; return this; }
        @Override public Builder key(int v) { this.key = v; return this; }

        @Override
        public KeyEvent build() {
            return new KeyEventImpl(down, key);
        }
    }
}
