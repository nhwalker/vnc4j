package io.github.nhwalker.vnc4j.protocol.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Package-private helpers for endianness-aware GII I/O. */
final class GiiIo {
    private GiiIo() {}

    static void writeEU16(DataOutputStream dos, boolean bigEndian, int value) throws IOException {
        if (bigEndian) {
            dos.writeShort(value);
        } else {
            dos.writeByte(value & 0xFF);
            dos.writeByte((value >> 8) & 0xFF);
        }
    }

    static int readEU16(DataInputStream dis, boolean bigEndian) throws IOException {
        int b0 = dis.readUnsignedByte();
        int b1 = dis.readUnsignedByte();
        return bigEndian ? (b0 << 8) | b1 : (b1 << 8) | b0;
    }

    static void writeEU32(DataOutputStream dos, boolean bigEndian, long value) throws IOException {
        int v = (int) (value & 0xFFFFFFFFL);
        if (bigEndian) {
            dos.writeInt(v);
        } else {
            dos.writeByte(v & 0xFF);
            dos.writeByte((v >> 8) & 0xFF);
            dos.writeByte((v >> 16) & 0xFF);
            dos.writeByte((v >> 24) & 0xFF);
        }
    }

    static long readEU32(DataInputStream dis, boolean bigEndian) throws IOException {
        int b0 = dis.readUnsignedByte();
        int b1 = dis.readUnsignedByte();
        int b2 = dis.readUnsignedByte();
        int b3 = dis.readUnsignedByte();
        int v = bigEndian ? (b0 << 24) | (b1 << 16) | (b2 << 8) | b3
                          : (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
        return Integer.toUnsignedLong(v);
    }

    static void writeES32(DataOutputStream dos, boolean bigEndian, int value) throws IOException {
        if (bigEndian) {
            dos.writeInt(value);
        } else {
            dos.writeByte(value & 0xFF);
            dos.writeByte((value >> 8) & 0xFF);
            dos.writeByte((value >> 16) & 0xFF);
            dos.writeByte((value >> 24) & 0xFF);
        }
    }

    static int readES32(DataInputStream dis, boolean bigEndian) throws IOException {
        int b0 = dis.readUnsignedByte();
        int b1 = dis.readUnsignedByte();
        int b2 = dis.readUnsignedByte();
        int b3 = dis.readUnsignedByte();
        return bigEndian ? (b0 << 24) | (b1 << 16) | (b2 << 8) | b3
                         : (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
    }
}
