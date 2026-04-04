package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleTightPngJpegImpl;

/** TightPNG JpegCompression rectangle (encoding type -260, compression type 0x9). */
public non-sealed interface RfbRectangleTightPngJpeg extends RfbRectangleTightPng {

    static Builder newBuilder() {
        return new RfbRectangleTightPngJpegImpl.BuilderImpl();
    }

    byte[] jpegData();

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder streamResets(int streamResets);
        Builder jpegData(byte[] jpegData);

        RfbRectangleTightPngJpeg build();

        default Builder from(RfbRectangleTightPngJpeg obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height())
                    .streamResets(obj.streamResets()).jpegData(obj.jpegData());
        }
    }
}
