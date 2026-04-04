package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiDeviceDestruction;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public record GiiDeviceDestructionImpl(boolean bigEndian, long deviceOrigin) implements GiiDeviceDestruction {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(253); // message-type
        dos.writeByte(bigEndian ? 0x83 : 0x03); // endian-and-sub-type
        GiiIo.writeEU16(dos, bigEndian, 4); // length = 4
        GiiIo.writeEU32(dos, bigEndian, deviceOrigin);
    }

    public static GiiDeviceDestruction read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        int endianAndSubType = dis.readUnsignedByte();
        boolean bigEndian = (endianAndSubType & 0x80) != 0;
        int length = GiiIo.readEU16(dis, bigEndian); // should be 4
        long deviceOrigin = GiiIo.readEU32(dis, bigEndian);
        return new GiiDeviceDestructionImpl(bigEndian, deviceOrigin);
    }

    public static final class BuilderImpl implements GiiDeviceDestruction.Builder {
        private boolean bigEndian;
        private long deviceOrigin;

        @Override public Builder bigEndian(boolean v) { this.bigEndian = v; return this; }
        @Override public Builder deviceOrigin(long v) { this.deviceOrigin = v; return this; }

        @Override
        public GiiDeviceDestruction build() {
            return new GiiDeviceDestructionImpl(bigEndian, deviceOrigin);
        }
    }
}
