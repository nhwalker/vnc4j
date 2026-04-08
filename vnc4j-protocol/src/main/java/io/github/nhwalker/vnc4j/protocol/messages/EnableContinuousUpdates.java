package io.github.nhwalker.vnc4j.protocol.messages;

import io.github.nhwalker.vnc4j.protocol.internal.EnableContinuousUpdatesImpl;

/** Client message to enable or disable continuous framebuffer updates for a region. */
public non-sealed interface EnableContinuousUpdates extends ClientMessage {
    static Builder newBuilder() {
        return new EnableContinuousUpdatesImpl.BuilderImpl();
    }

    boolean enable();
    int x();
    int y();
    int width();
    int height();

    interface Builder {
        Builder enable(boolean enable);
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);

        EnableContinuousUpdates build();

        default Builder from(EnableContinuousUpdates msg) {
            return enable(msg.enable()).x(msg.x()).y(msg.y()).width(msg.width()).height(msg.height());
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static EnableContinuousUpdates read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.EnableContinuousUpdatesImpl.read(in);
    }
}
