package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.messages.PixelFormat;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Utility methods shared by Tight and TightPNG encoding implementations. */
final class TightIo {
    private TightIo() {}

    /**
     * Returns the TPIXEL byte count for the given pixel format.
     * Per the Tight encoding spec, TPIXEL is 3 bytes when true-colour-flag is set,
     * bits-per-pixel is 32, depth is 24, and each RGB channel is exactly 8 bits wide
     * (i.e. redMax == greenMax == blueMax == 255). Otherwise it is bitsPerPixel/8.
     */
    static int tpixelSize(PixelFormat pf) {
        if (pf.trueColour() && pf.bitsPerPixel() == 32 && pf.depth() == 24
                && pf.redMax() == 255 && pf.greenMax() == 255 && pf.blueMax() == 255) {
            return 3;
        }
        return pf.bitsPerPixel() / 8;
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
