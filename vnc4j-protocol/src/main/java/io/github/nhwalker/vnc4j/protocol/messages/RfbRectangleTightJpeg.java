package io.github.nhwalker.vnc4j.protocol.messages;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleTightJpegImpl;

/**
 * Tight JpegCompression rectangle (encoding type 7, compression type 0x9).
 * The pixel data is a JPEG image.
 */
public non-sealed interface RfbRectangleTightJpeg extends RfbRectangleTight {

    static Builder newBuilder() {
        return new RfbRectangleTightJpegImpl.BuilderImpl();
    }

    /** JPEG-encoded pixel data (preceded on the wire by a compact-length prefix). */
    byte[] jpegData();

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder streamResets(int streamResets);
        Builder jpegData(byte[] jpegData);

        RfbRectangleTightJpeg build();

        default Builder from(RfbRectangleTightJpeg obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height())
                    .streamResets(obj.streamResets()).jpegData(obj.jpegData());
        }
    }
}
