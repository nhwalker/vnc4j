package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleRawImpl;

/** Raw pixel data rectangle (encoding type 0). */
public non-sealed interface RfbRectangleRaw extends RfbRectangle {
    int ENCODING_TYPE = 0;

    static Builder newBuilder() {
        return new RfbRectangleRawImpl.BuilderImpl();
    }

    /** Raw pixel data: {@code width × height × bytesPerPixel} bytes. */
    byte[] pixels();

    @Override
    default int encodingType() { return ENCODING_TYPE; }

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder pixels(byte[] pixels);

        RfbRectangleRaw build();

        default Builder from(RfbRectangleRaw obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height())
                    .pixels(obj.pixels());
        }
    }
}
