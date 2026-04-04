package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleImpl;

/** Represents a single encoded rectangle within a FramebufferUpdate. */
public interface RfbRectangle {
    static Builder newBuilder() {
        return new RfbRectangleImpl.BuilderImpl();
    }

    int x();
    int y();
    int width();
    int height();
    int encodingType();
    byte[] data();

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder encodingType(int encodingType);
        Builder data(byte[] data);

        RfbRectangle build();

        default Builder from(RfbRectangle msg) {
            return x(msg.x()).y(msg.y()).width(msg.width()).height(msg.height())
                    .encodingType(msg.encodingType()).data(msg.data());
        }
    }
}
