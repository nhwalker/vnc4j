package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleTightFillImpl;

/**
 * Tight FillCompression rectangle (encoding type 7, compression type 0x8).
 * The entire rectangle is filled with a single colour.
 */
public non-sealed interface RfbRectangleTightFill extends RfbRectangleTight {

    static Builder newBuilder() {
        return new RfbRectangleTightFillImpl.BuilderImpl();
    }

    /**
     * Fill colour as a TPIXEL: 3 bytes when {@code bitsPerPixel=32 & depth=24},
     * otherwise {@code bitsPerPixel/8} bytes.
     */
    byte[] fillColor();

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder streamResets(int streamResets);
        Builder fillColor(byte[] fillColor);

        RfbRectangleTightFill build();

        default Builder from(RfbRectangleTightFill obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height())
                    .streamResets(obj.streamResets()).fillColor(obj.fillColor());
        }
    }
}
