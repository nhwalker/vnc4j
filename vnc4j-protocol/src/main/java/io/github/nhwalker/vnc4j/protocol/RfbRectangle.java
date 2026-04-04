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

    void write(java.io.OutputStream out) throws java.io.IOException;

    /**
     * Reads an RfbRectangle header (x, y, width, height, encodingType) from the stream.
     * The data field will be an empty byte array; the caller must supply encoding-specific data separately.
     */
    static RfbRectangle read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleImpl.read(in);
    }
}
