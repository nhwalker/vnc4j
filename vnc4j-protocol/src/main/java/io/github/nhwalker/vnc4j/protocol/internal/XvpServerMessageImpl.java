package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.XvpServerMessage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public record XvpServerMessageImpl(int xvpVersion, int xvpMessageCode) implements XvpServerMessage {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(250); // message-type
        dos.writeByte(0); // padding
        dos.writeByte(xvpVersion);
        dos.writeByte(xvpMessageCode);
    }

    public static XvpServerMessage read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        dis.readUnsignedByte(); // padding
        int version = dis.readUnsignedByte();
        int code = dis.readUnsignedByte();
        return new XvpServerMessageImpl(version, code);
    }

    public static final class BuilderImpl implements XvpServerMessage.Builder {
        private int xvpVersion;
        private int xvpMessageCode;

        @Override public Builder xvpVersion(int v) { this.xvpVersion = v; return this; }
        @Override public Builder xvpMessageCode(int v) { this.xvpMessageCode = v; return this; }

        @Override
        public XvpServerMessage build() {
            return new XvpServerMessageImpl(xvpVersion, xvpMessageCode);
        }
    }
}
