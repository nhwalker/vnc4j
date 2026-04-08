package io.github.nhwalker.vnc4j.protocol.messages;

import io.github.nhwalker.vnc4j.protocol.internal.FramebufferUpdateRequestImpl;

/** Client request for a framebuffer update for the specified region. */
public non-sealed interface FramebufferUpdateRequest extends ClientMessage {
    static Builder newBuilder() {
        return new FramebufferUpdateRequestImpl.BuilderImpl();
    }

    boolean incremental();
    int x();
    int y();
    int width();
    int height();

    interface Builder {
        Builder incremental(boolean incremental);
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);

        FramebufferUpdateRequest build();

        default Builder from(FramebufferUpdateRequest msg) {
            return incremental(msg.incremental()).x(msg.x()).y(msg.y()).width(msg.width()).height(msg.height());
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static FramebufferUpdateRequest read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.FramebufferUpdateRequestImpl.read(in);
    }
}
