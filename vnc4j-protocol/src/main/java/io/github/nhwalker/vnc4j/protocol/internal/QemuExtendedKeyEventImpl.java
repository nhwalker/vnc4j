package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.QemuExtendedKeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public record QemuExtendedKeyEventImpl(int downFlag, int keysym, int keycode) implements QemuExtendedKeyEvent {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(255); // message-type
        dos.writeByte(0);   // sub-type
        dos.writeShort(downFlag);
        dos.writeInt(keysym);
        dos.writeInt(keycode);
    }

    public static QemuExtendedKeyEvent read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        // sub-type already read; read the rest
        int downFlag = dis.readUnsignedShort();
        int keysym = dis.readInt();
        int keycode = dis.readInt();
        return new QemuExtendedKeyEventImpl(downFlag, keysym, keycode);
    }

    public static final class BuilderImpl implements QemuExtendedKeyEvent.Builder {
        private int downFlag;
        private int keysym;
        private int keycode;

        @Override public Builder downFlag(int v) { this.downFlag = v; return this; }
        @Override public Builder keysym(int v) { this.keysym = v; return this; }
        @Override public Builder keycode(int v) { this.keycode = v; return this; }

        @Override
        public QemuExtendedKeyEvent build() {
            return new QemuExtendedKeyEventImpl(downFlag, keysym, keycode);
        }
    }
}
