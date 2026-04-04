package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.ClientCutText;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public final class ClientCutTextImpl implements ClientCutText {
    private final byte[] text;

    public ClientCutTextImpl(byte[] text) {
        this.text = text;
    }

    @Override
    public byte[] text() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientCutText other)) return false;
        return Arrays.equals(text, other.text());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(text);
    }

    @Override
    public String toString() {
        return "ClientCutText[text=" + Arrays.toString(text) + "]";
    }

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] t = text != null ? text : new byte[0];
        dos.writeByte(6); // message-type
        dos.writeByte(0); // padding
        dos.writeByte(0);
        dos.writeByte(0);
        dos.writeInt(t.length);
        dos.write(t);
    }

    public static ClientCutText read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        dis.readUnsignedByte(); // padding
        dis.readUnsignedByte();
        dis.readUnsignedByte();
        int len = dis.readInt();
        byte[] text = new byte[len];
        dis.readFully(text);
        return new ClientCutTextImpl(text);
    }

    public static final class BuilderImpl implements ClientCutText.Builder {
        private byte[] text;

        @Override
        public Builder text(byte[] text) {
            this.text = text;
            return this;
        }

        @Override
        public ClientCutText build() {
            return new ClientCutTextImpl(text);
        }
    }
}
