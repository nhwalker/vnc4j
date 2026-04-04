package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleTightPngFillImpl;

/** TightPNG FillCompression rectangle (encoding type -260, compression type 0x8). */
public non-sealed interface RfbRectangleTightPngFill extends RfbRectangleTightPng {

    static Builder newBuilder() {
        return new RfbRectangleTightPngFillImpl.BuilderImpl();
    }

    byte[] fillColor();

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder streamResets(int streamResets);
        Builder fillColor(byte[] fillColor);

        RfbRectangleTightPngFill build();

        default Builder from(RfbRectangleTightPngFill obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height())
                    .streamResets(obj.streamResets()).fillColor(obj.fillColor());
        }
    }
}
