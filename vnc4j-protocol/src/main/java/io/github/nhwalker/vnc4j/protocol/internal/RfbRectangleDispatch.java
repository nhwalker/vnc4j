package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangle;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleTight;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleTightPng;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads the 10-byte rectangle header and dispatches to the appropriate
 * per-encoding {@code readPayload()} method.
 */
public final class RfbRectangleDispatch {
    private RfbRectangleDispatch() {}

    public static RfbRectangle read(InputStream in, PixelFormat pf) throws IOException {
        DataInputStream dis = (in instanceof DataInputStream d) ? d : new DataInputStream(in);
        int x   = dis.readUnsignedShort();
        int y   = dis.readUnsignedShort();
        int w   = dis.readUnsignedShort();
        int h   = dis.readUnsignedShort();
        int enc = dis.readInt();
        return switch (enc) {
            case 0    -> RfbRectangleRawImpl.readPayload(dis, x, y, w, h, pf);
            case 1    -> RfbRectangleCopyRectImpl.readPayload(dis, x, y, w, h, pf);
            case 2    -> RfbRectangleRreImpl.readPayload(dis, x, y, w, h, pf);
            case 5    -> RfbRectangleHextileImpl.readPayload(dis, x, y, w, h, pf);
            case 6    -> RfbRectangleZlibImpl.readPayload(dis, x, y, w, h, pf);
            case 7    -> readTight(dis, x, y, w, h, pf);
            case 16   -> RfbRectangleZrleImpl.readPayload(dis, x, y, w, h, pf);
            case 21   -> throw new UnsupportedOperationException(
                                "JPEG encoding (type 21) has no length prefix; cannot self-delimit on read");
            case -260 -> readTightPng(dis, x, y, w, h, pf);
            case -223 -> RfbRectangleDesktopSizeImpl.readPayload(dis, x, y, w, h, pf);
            case -224 -> RfbRectangleLastRectImpl.readPayload(dis, x, y, w, h, pf);
            case -239 -> RfbRectangleCursorImpl.readPayload(dis, x, y, w, h, pf);
            case -308 -> RfbRectangleExtendedDesktopSizeImpl.readPayload(dis, x, y, w, h, pf);
            default   -> throw new UnsupportedOperationException("Unknown encoding type: " + enc);
        };
    }

    private static RfbRectangleTight readTight(DataInputStream dis, int x, int y, int w, int h,
            PixelFormat pf) throws IOException {
        int ctrl = dis.readUnsignedByte();
        int streamResets = ctrl & 0xF;
        int highNibble = (ctrl >> 4) & 0xF;
        return switch (highNibble) {
            case 0x8 -> RfbRectangleTightFillImpl.readPayload(dis, x, y, w, h, pf, streamResets);
            case 0x9 -> RfbRectangleTightJpegImpl.readPayload(dis, x, y, w, h, pf, streamResets);
            default  -> RfbRectangleTightBasicImpl.readPayload(dis, x, y, w, h, pf, ctrl);
        };
    }

    private static RfbRectangleTightPng readTightPng(DataInputStream dis, int x, int y, int w, int h,
            PixelFormat pf) throws IOException {
        int ctrl = dis.readUnsignedByte();
        int streamResets = ctrl & 0xF;
        int highNibble = (ctrl >> 4) & 0xF;
        return switch (highNibble) {
            case 0x8 -> RfbRectangleTightPngFillImpl.readPayload(dis, x, y, w, h, pf, streamResets);
            case 0x9 -> RfbRectangleTightPngJpegImpl.readPayload(dis, x, y, w, h, pf, streamResets);
            case 0xA -> RfbRectangleTightPngPngImpl.readPayload(dis, x, y, w, h, pf, streamResets);
            default  -> throw new UnsupportedOperationException(
                                "Unknown TightPNG compression type: " + highNibble);
        };
    }
}
