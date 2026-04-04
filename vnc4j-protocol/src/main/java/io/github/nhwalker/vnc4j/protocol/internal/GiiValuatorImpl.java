package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiValuator;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public record GiiValuatorImpl(
        long index,
        String longName,
        String shortName,
        int rangeMin,
        int rangeCenter,
        int rangeMax,
        long siUnit,
        int siAdd,
        int siMul,
        int siDiv,
        int siShift
) implements GiiValuator {

    @Override
    public void write(OutputStream out, boolean bigEndian) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        GiiIo.writeEU32(dos, bigEndian, index);
        // longName: 74 bytes + 1 NUL terminator = 75 bytes
        writeFixedUtf8(dos, longName, 74);
        dos.writeByte(0); // NUL terminator
        // shortName: 4 bytes + 1 NUL = 5 bytes
        writeFixedUtf8(dos, shortName, 4);
        dos.writeByte(0); // NUL terminator
        GiiIo.writeES32(dos, bigEndian, rangeMin);
        GiiIo.writeES32(dos, bigEndian, rangeCenter);
        GiiIo.writeES32(dos, bigEndian, rangeMax);
        GiiIo.writeEU32(dos, bigEndian, siUnit);
        GiiIo.writeES32(dos, bigEndian, siAdd);
        GiiIo.writeES32(dos, bigEndian, siMul);
        GiiIo.writeES32(dos, bigEndian, siDiv);
        GiiIo.writeES32(dos, bigEndian, siShift);
    }

    private static void writeFixedUtf8(DataOutputStream dos, String s, int maxBytes) throws IOException {
        byte[] bytes = s != null ? s.getBytes(StandardCharsets.UTF_8) : new byte[0];
        int len = Math.min(bytes.length, maxBytes);
        dos.write(bytes, 0, len);
        for (int i = len; i < maxBytes; i++) {
            dos.writeByte(0);
        }
    }

    public static GiiValuator read(InputStream in, boolean bigEndian) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        long index = GiiIo.readEU32(dis, bigEndian);
        byte[] longNameBuf = new byte[74];
        dis.readFully(longNameBuf);
        dis.readUnsignedByte(); // NUL terminator
        String longName = readFixedUtf8(longNameBuf);
        byte[] shortNameBuf = new byte[4];
        dis.readFully(shortNameBuf);
        dis.readUnsignedByte(); // NUL terminator
        String shortName = readFixedUtf8(shortNameBuf);
        int rangeMin = GiiIo.readES32(dis, bigEndian);
        int rangeCenter = GiiIo.readES32(dis, bigEndian);
        int rangeMax = GiiIo.readES32(dis, bigEndian);
        long siUnit = GiiIo.readEU32(dis, bigEndian);
        int siAdd = GiiIo.readES32(dis, bigEndian);
        int siMul = GiiIo.readES32(dis, bigEndian);
        int siDiv = GiiIo.readES32(dis, bigEndian);
        int siShift = GiiIo.readES32(dis, bigEndian);
        return new GiiValuatorImpl(index, longName, shortName, rangeMin, rangeCenter, rangeMax,
                siUnit, siAdd, siMul, siDiv, siShift);
    }

    private static String readFixedUtf8(byte[] buf) {
        int len = 0;
        while (len < buf.length && buf[len] != 0) len++;
        return new String(buf, 0, len, StandardCharsets.UTF_8);
    }

    public static final class BuilderImpl implements GiiValuator.Builder {
        private long index;
        private String longName;
        private String shortName;
        private int rangeMin;
        private int rangeCenter;
        private int rangeMax;
        private long siUnit;
        private int siAdd;
        private int siMul;
        private int siDiv;
        private int siShift;

        @Override public Builder index(long v) { this.index = v; return this; }
        @Override public Builder longName(String v) { this.longName = v; return this; }
        @Override public Builder shortName(String v) { this.shortName = v; return this; }
        @Override public Builder rangeMin(int v) { this.rangeMin = v; return this; }
        @Override public Builder rangeCenter(int v) { this.rangeCenter = v; return this; }
        @Override public Builder rangeMax(int v) { this.rangeMax = v; return this; }
        @Override public Builder siUnit(long v) { this.siUnit = v; return this; }
        @Override public Builder siAdd(int v) { this.siAdd = v; return this; }
        @Override public Builder siMul(int v) { this.siMul = v; return this; }
        @Override public Builder siDiv(int v) { this.siDiv = v; return this; }
        @Override public Builder siShift(int v) { this.siShift = v; return this; }

        @Override
        public GiiValuator build() {
            return new GiiValuatorImpl(index, longName, shortName, rangeMin, rangeCenter,
                    rangeMax, siUnit, siAdd, siMul, siDiv, siShift);
        }
    }
}
