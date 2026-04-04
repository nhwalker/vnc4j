package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Utility methods shared by Tight and TightPNG encoding implementations. */
final class TightIo {
    private TightIo() {}

    /**
     * Returns the TPIXEL byte count for the given pixel format:
     * 3 bytes when bitsPerPixel=32 and depth=24; otherwise bitsPerPixel/8.
     */
    static int tpixelSize(PixelFormat pf) {
        return (pf.bitsPerPixel() == 32 && pf.depth() == 24) ? 3 : pf.bitsPerPixel() / 8;
    }

    /** Reads a Tight compact length (1-3 bytes) from the stream. */
    static int readCompactLength(DataInputStream dis) throws IOException {
        int b0 = dis.readUnsignedByte();
        int length = b0 & 0x7F;
        if ((b0 & 0x80) != 0) {
            int b1 = dis.readUnsignedByte();
            length |= (b1 & 0x7F) << 7;
            if ((b1 & 0x80) != 0) {
                int b2 = dis.readUnsignedByte();
                length |= b2 << 14;
            }
        }
        return length;
    }

    /** Writes a Tight compact length (1-3 bytes) to the stream. */
    static void writeCompactLength(DataOutputStream dos, int length) throws IOException {
        if (length < 128) {
            dos.writeByte(length);
        } else if (length < 16384) {
            dos.writeByte((length & 0x7F) | 0x80);
            dos.writeByte((length >> 7) & 0x7F);
        } else {
            dos.writeByte((length & 0x7F) | 0x80);
            dos.writeByte(((length >> 7) & 0x7F) | 0x80);
            dos.writeByte((length >> 14) & 0xFF);
        }
    }
}
