package io.github.nhwalker.vnc4j.protocol.messages;

import io.github.nhwalker.vnc4j.protocol.internal.ScreenImpl;

/** Represents a single screen in a multi-monitor desktop layout. */
public interface Screen {
    static Builder newBuilder() {
        return new ScreenImpl.BuilderImpl();
    }

    long id();
    int x();
    int y();
    int width();
    int height();
    long flags();

    interface Builder {
        Builder id(long id);
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder flags(long flags);

        Screen build();

        default Builder from(Screen msg) {
            return id(msg.id()).x(msg.x()).y(msg.y()).width(msg.width()).height(msg.height()).flags(msg.flags());
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static Screen read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.ScreenImpl.read(in);
    }
}
