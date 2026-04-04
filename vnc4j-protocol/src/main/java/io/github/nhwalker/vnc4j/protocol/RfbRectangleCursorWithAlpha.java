package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleCursorWithAlphaImpl;

/**
 * CursorWithAlpha pseudo-encoding rectangle (encoding type -314). Delivers a cursor with
 * a full alpha channel. The pixel data is always 32-bit RGBA regardless of the session
 * pixel format, encoded according to the nested {@link #encoding()} type.
 */
public non-sealed interface RfbRectangleCursorWithAlpha extends RfbRectangle {
    int ENCODING_TYPE = -314;

    static Builder newBuilder() {
        return new RfbRectangleCursorWithAlphaImpl.BuilderImpl();
    }

    /** Nested encoding type that describes how {@link #data()} is encoded (e.g. 0 = Raw). */
    int encoding();
    /**
     * Encoded 32-bit RGBA pixel data: {@code width × height × 4} bytes once decoded.
     * Stored here as the raw (possibly compressed) bytes for the given {@link #encoding()}.
     */
    byte[] data();

    @Override
    default int encodingType() { return ENCODING_TYPE; }

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder encoding(int encoding);
        Builder data(byte[] data);

        RfbRectangleCursorWithAlpha build();

        default Builder from(RfbRectangleCursorWithAlpha obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height())
                    .encoding(obj.encoding()).data(obj.data());
        }
    }
}
