package io.github.nhwalker.vnc4j.protocol.messages;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleZrleImpl;

/**
 * ZRLE rectangle (encoding type 16). Uses zlib-compressed tiled pixel data;
 * the compressed bytes are stored as-is.
 */
public non-sealed interface RfbRectangleZrle extends RfbRectangle {
    int ENCODING_TYPE = 16;

    static Builder newBuilder() {
        return new RfbRectangleZrleImpl.BuilderImpl();
    }

    /** Zlib-compressed ZRLE data (preceded by a U32 length on the wire). */
    byte[] zlibData();

    @Override
    default int encodingType() { return ENCODING_TYPE; }

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder zlibData(byte[] zlibData);

        RfbRectangleZrle build();

        default Builder from(RfbRectangleZrle obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height())
                    .zlibData(obj.zlibData());
        }
    }
}
