package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.XvpClientMessage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public record XvpClientMessageImpl(int xvpVersion, int xvpMessageCode) implements XvpClientMessage {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(250); // message-type
        dos.writeByte(0); // padding
        dos.writeByte(xvpVersion);
        dos.writeByte(xvpMessageCode);
    }

    public static XvpClientMessage read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        dis.readUnsignedByte(); // padding
        int version = dis.readUnsignedByte();
        int code = dis.readUnsignedByte();
        return new XvpClientMessageImpl(version, code);
    }

    public static final class BuilderImpl implements XvpClientMessage.Builder {
        private int xvpVersion;
        private int xvpMessageCode;

        @Override public Builder xvpVersion(int v) { this.xvpVersion = v; return this; }
        @Override public Builder xvpMessageCode(int v) { this.xvpMessageCode = v; return this; }

        @Override
        public XvpClientMessage build() {
            return new XvpClientMessageImpl(xvpVersion, xvpMessageCode);
        }
    }
}
