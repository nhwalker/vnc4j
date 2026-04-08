package io.github.nhwalker.vnc4j.protocol.messages;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleLastRectImpl;

/**
 * LastRect pseudo-encoding rectangle (encoding type -224). Signals that no more rectangles
 * follow in the current FramebufferUpdate, even if the rectangle count says otherwise.
 * Carries no payload.
 */
public non-sealed interface RfbRectangleLastRect extends RfbRectangle {
    int ENCODING_TYPE = -224;

    static Builder newBuilder() {
        return new RfbRectangleLastRectImpl.BuilderImpl();
    }

    @Override
    default int encodingType() { return ENCODING_TYPE; }

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);

        RfbRectangleLastRect build();

        default Builder from(RfbRectangleLastRect obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height());
        }
    }
}
