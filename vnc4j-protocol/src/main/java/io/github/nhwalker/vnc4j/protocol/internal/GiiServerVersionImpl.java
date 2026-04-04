package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiServerVersion;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public record GiiServerVersionImpl(boolean bigEndian, int maximumVersion, int minimumVersion) implements GiiServerVersion {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(253); // message-type
        dos.writeByte(bigEndian ? 0x81 : 0x01); // endian-and-sub-type
        GiiIo.writeEU16(dos, bigEndian, 4); // length = 4
        GiiIo.writeEU16(dos, bigEndian, maximumVersion);
        GiiIo.writeEU16(dos, bigEndian, minimumVersion);
    }

    public static GiiServerVersion read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        int endianAndSubType = dis.readUnsignedByte();
        boolean bigEndian = (endianAndSubType & 0x80) != 0;
        int length = GiiIo.readEU16(dis, bigEndian); // should be 4
        int maximumVersion = GiiIo.readEU16(dis, bigEndian);
        int minimumVersion = GiiIo.readEU16(dis, bigEndian);
        return new GiiServerVersionImpl(bigEndian, maximumVersion, minimumVersion);
    }

    public static final class BuilderImpl implements GiiServerVersion.Builder {
        private boolean bigEndian;
        private int maximumVersion;
        private int minimumVersion;

        @Override public Builder bigEndian(boolean v) { this.bigEndian = v; return this; }
        @Override public Builder maximumVersion(int v) { this.maximumVersion = v; return this; }
        @Override public Builder minimumVersion(int v) { this.minimumVersion = v; return this; }

        @Override
        public GiiServerVersion build() {
            return new GiiServerVersionImpl(bigEndian, maximumVersion, minimumVersion);
        }
    }
}
