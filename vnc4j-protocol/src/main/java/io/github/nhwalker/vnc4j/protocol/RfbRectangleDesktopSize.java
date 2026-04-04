package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleDesktopSizeImpl;

/**
 * DesktopSize pseudo-encoding rectangle (encoding type -223). Carries no payload;
 * the new framebuffer dimensions are in {@link #width()} and {@link #height()}.
 */
public non-sealed interface RfbRectangleDesktopSize extends RfbRectangle {
    int ENCODING_TYPE = -223;

    static Builder newBuilder() {
        return new RfbRectangleDesktopSizeImpl.BuilderImpl();
    }

    @Override
    default int encodingType() { return ENCODING_TYPE; }

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);

        RfbRectangleDesktopSize build();

        default Builder from(RfbRectangleDesktopSize obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height());
        }
    }
}
