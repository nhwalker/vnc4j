package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiDeviceCreation;
import io.github.nhwalker.vnc4j.protocol.GiiValuator;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public record GiiDeviceCreationImpl(
        boolean bigEndian,
        String deviceName,
        long vendorId,
        long productId,
        long canGenerate,
        long numRegisters,
        long numButtons,
        List<GiiValuator> valuators
) implements GiiDeviceCreation {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        List<GiiValuator> vals = valuators != null ? valuators : List.of();
        int length = 56 + vals.size() * 116;
        dos.writeByte(253); // message-type
        dos.writeByte(bigEndian ? 0x82 : 0x02); // endian-and-sub-type
        GiiIo.writeEU16(dos, bigEndian, length);
        // device name: 31 bytes + 1 NUL
        byte[] nameBytes = deviceName != null ? deviceName.getBytes(StandardCharsets.UTF_8) : new byte[0];
        int nameLen = Math.min(nameBytes.length, 31);
        dos.write(nameBytes, 0, nameLen);
        for (int i = nameLen; i < 31; i++) dos.writeByte(0);
        dos.writeByte(0); // NUL terminator
        GiiIo.writeEU32(dos, bigEndian, vendorId);
        GiiIo.writeEU32(dos, bigEndian, productId);
        GiiIo.writeEU32(dos, bigEndian, canGenerate);
        GiiIo.writeEU32(dos, bigEndian, numRegisters);
        GiiIo.writeEU32(dos, bigEndian, (long) vals.size());
        GiiIo.writeEU32(dos, bigEndian, numButtons);
        for (GiiValuator v : vals) {
            v.write(out, bigEndian);
        }
    }

    public static GiiDeviceCreation read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        int endianAndSubType = dis.readUnsignedByte();
        boolean bigEndian = (endianAndSubType & 0x80) != 0;
        int length = GiiIo.readEU16(dis, bigEndian);
        // device name: 31 bytes + 1 NUL
        byte[] nameBuf = new byte[31];
        dis.readFully(nameBuf);
        dis.readUnsignedByte(); // NUL terminator
        int nameLen = 0;
        while (nameLen < 31 && nameBuf[nameLen] != 0) nameLen++;
        String deviceName = new String(nameBuf, 0, nameLen, StandardCharsets.UTF_8);
        long vendorId = GiiIo.readEU32(dis, bigEndian);
        long productId = GiiIo.readEU32(dis, bigEndian);
        long canGenerate = GiiIo.readEU32(dis, bigEndian);
        long numRegisters = GiiIo.readEU32(dis, bigEndian);
        long numValuators = GiiIo.readEU32(dis, bigEndian);
        long numButtons = GiiIo.readEU32(dis, bigEndian);
        List<GiiValuator> valuators = new ArrayList<>((int) numValuators);
        for (int i = 0; i < numValuators; i++) {
            valuators.add(GiiValuator.read(in, bigEndian));
        }
        return new GiiDeviceCreationImpl(bigEndian, deviceName, vendorId, productId,
                canGenerate, numRegisters, numButtons, valuators);
    }

    public static final class BuilderImpl implements GiiDeviceCreation.Builder {
        private boolean bigEndian;
        private String deviceName;
        private long vendorId;
        private long productId;
        private long canGenerate;
        private long numRegisters;
        private long numButtons;
        private List<GiiValuator> valuators;

        @Override public Builder bigEndian(boolean v) { this.bigEndian = v; return this; }
        @Override public Builder deviceName(String v) { this.deviceName = v; return this; }
        @Override public Builder vendorId(long v) { this.vendorId = v; return this; }
        @Override public Builder productId(long v) { this.productId = v; return this; }
        @Override public Builder canGenerate(long v) { this.canGenerate = v; return this; }
        @Override public Builder numRegisters(long v) { this.numRegisters = v; return this; }
        @Override public Builder numButtons(long v) { this.numButtons = v; return this; }
        @Override public Builder valuators(List<GiiValuator> v) { this.valuators = v; return this; }

        @Override
        public GiiDeviceCreation build() {
            return new GiiDeviceCreationImpl(bigEndian, deviceName, vendorId, productId,
                    canGenerate, numRegisters, numButtons, valuators);
        }
    }
}
