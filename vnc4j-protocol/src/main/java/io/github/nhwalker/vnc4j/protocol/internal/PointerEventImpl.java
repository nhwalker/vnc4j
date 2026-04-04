package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.PointerEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public record PointerEventImpl(int buttonMask, int x, int y) implements PointerEvent {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(5); // message-type
        dos.writeByte(buttonMask);
        dos.writeShort(x);
        dos.writeShort(y);
    }

    public static PointerEvent read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        int buttonMask = dis.readUnsignedByte();
        int x = dis.readUnsignedShort();
        int y = dis.readUnsignedShort();
        return new PointerEventImpl(buttonMask, x, y);
    }

    public static final class BuilderImpl implements PointerEvent.Builder {
        private int buttonMask;
        private int x;
        private int y;

        @Override public Builder buttonMask(int v) { this.buttonMask = v; return this; }
        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }

        @Override
        public PointerEvent build() {
            return new PointerEventImpl(buttonMask, x, y);
        }
    }
}
