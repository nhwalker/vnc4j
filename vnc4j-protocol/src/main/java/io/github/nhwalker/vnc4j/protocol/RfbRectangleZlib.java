package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleZlibImpl;

/**
 * Zlib rectangle (encoding type 6). Pixel data is zlib-compressed Raw encoding;
 * the compressed bytes are stored as-is.
 */
public non-sealed interface RfbRectangleZlib extends RfbRectangle {
    int ENCODING_TYPE = 6;

    static Builder newBuilder() {
        return new RfbRectangleZlibImpl.BuilderImpl();
    }

    /** Zlib-compressed raw pixel data (preceded by a U32 length on the wire). */
    byte[] zlibData();

    @Override
    default int encodingType() { return ENCODING_TYPE; }

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder zlibData(byte[] zlibData);

        RfbRectangleZlib build();

        default Builder from(RfbRectangleZlib obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height())
                    .zlibData(obj.zlibData());
        }
    }
}
