package io.github.nhwalker.vnc4j.protocol.messages;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleCopyRectImpl;

/** CopyRect rectangle (encoding type 1): copies from a source position in the framebuffer. */
public non-sealed interface RfbRectangleCopyRect extends RfbRectangle {
    int ENCODING_TYPE = 1;

    static Builder newBuilder() {
        return new RfbRectangleCopyRectImpl.BuilderImpl();
    }

    int srcX();
    int srcY();

    @Override
    default int encodingType() { return ENCODING_TYPE; }

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder srcX(int srcX);
        Builder srcY(int srcY);

        RfbRectangleCopyRect build();

        default Builder from(RfbRectangleCopyRect obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height())
                    .srcX(obj.srcX()).srcY(obj.srcY());
        }
    }
}
