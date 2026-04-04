package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.SetPixelFormatImpl;

/** Client message instructing the server to use a specific pixel format. */
public non-sealed interface SetPixelFormat extends ClientMessage {
    static Builder newBuilder() {
        return new SetPixelFormatImpl.BuilderImpl();
    }

    PixelFormat pixelFormat();

    interface Builder {
        Builder pixelFormat(PixelFormat pixelFormat);

        SetPixelFormat build();

        default Builder from(SetPixelFormat msg) {
            return pixelFormat(msg.pixelFormat());
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static SetPixelFormat read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.SetPixelFormatImpl.read(in);
    }
}
