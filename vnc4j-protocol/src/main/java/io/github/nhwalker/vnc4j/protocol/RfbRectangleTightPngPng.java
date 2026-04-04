package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleTightPngPngImpl;

/** TightPNG PngCompression rectangle (encoding type -260, compression type 0xA). */
public non-sealed interface RfbRectangleTightPngPng extends RfbRectangleTightPng {

    static Builder newBuilder() {
        return new RfbRectangleTightPngPngImpl.BuilderImpl();
    }

    /** PNG-encoded pixel data (preceded on the wire by a compact-length prefix). */
    byte[] pngData();

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder streamResets(int streamResets);
        Builder pngData(byte[] pngData);

        RfbRectangleTightPngPng build();

        default Builder from(RfbRectangleTightPngPng obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height())
                    .streamResets(obj.streamResets()).pngData(obj.pngData());
        }
    }
}
