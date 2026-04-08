package io.github.nhwalker.vnc4j.protocol.messages;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleCursorImpl;

/**
 * Cursor pseudo-encoding rectangle (encoding type -239). Delivers a cursor image.
 * The rectangle position ({@link #x()}, {@link #y()}) is the hotspot.
 */
public non-sealed interface RfbRectangleCursor extends RfbRectangle {
    int ENCODING_TYPE = -239;

    static Builder newBuilder() {
        return new RfbRectangleCursorImpl.BuilderImpl();
    }

    /** Cursor pixel data: {@code width × height × bytesPerPixel} bytes. */
    byte[] pixels();
    /** 1-bit-per-pixel mask: {@code ⌈width/8⌉ × height} bytes. */
    byte[] bitmask();

    @Override
    default int encodingType() { return ENCODING_TYPE; }

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder pixels(byte[] pixels);
        Builder bitmask(byte[] bitmask);

        RfbRectangleCursor build();

        default Builder from(RfbRectangleCursor obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height())
                    .pixels(obj.pixels()).bitmask(obj.bitmask());
        }
    }
}
