package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiClientVersion;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public record GiiClientVersionImpl(boolean bigEndian, int version) implements GiiClientVersion {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(253); // message-type
        dos.writeByte(bigEndian ? 0x81 : 0x01); // endian-and-sub-type
        GiiIo.writeEU16(dos, bigEndian, 2); // length = 2
        GiiIo.writeEU16(dos, bigEndian, version);
    }

    public static GiiClientVersion read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        int endianAndSubType = dis.readUnsignedByte();
        boolean bigEndian = (endianAndSubType & 0x80) != 0;
        int length = GiiIo.readEU16(dis, bigEndian); // should be 2
        int version = GiiIo.readEU16(dis, bigEndian);
        return new GiiClientVersionImpl(bigEndian, version);
    }

    public static final class BuilderImpl implements GiiClientVersion.Builder {
        private boolean bigEndian;
        private int version;

        @Override public Builder bigEndian(boolean v) { this.bigEndian = v; return this; }
        @Override public Builder version(int v) { this.version = v; return this; }

        @Override
        public GiiClientVersion build() {
            return new GiiClientVersionImpl(bigEndian, version);
        }
    }
}
