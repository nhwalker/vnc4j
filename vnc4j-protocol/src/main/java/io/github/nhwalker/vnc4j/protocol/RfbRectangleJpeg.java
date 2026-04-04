package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleJpegImpl;

/**
 * JPEG rectangle (encoding type 21). The wire format carries no length prefix before
 * the JPEG data, making it impossible to self-delimit on read; {@link RfbRectangle#read}
 * throws {@link UnsupportedOperationException} for this encoding type. Write is supported.
 */
public non-sealed interface RfbRectangleJpeg extends RfbRectangle {
    int ENCODING_TYPE = 21;

    static Builder newBuilder() {
        return new RfbRectangleJpegImpl.BuilderImpl();
    }

    byte[] data();

    @Override
    default int encodingType() { return ENCODING_TYPE; }

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder data(byte[] data);

        RfbRectangleJpeg build();

        default Builder from(RfbRectangleJpeg obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height())
                    .data(obj.data());
        }
    }
}
