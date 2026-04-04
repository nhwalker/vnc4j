package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.SetDesktopSizeImpl;

import java.util.List;

/** Client message requesting a desktop resize with a new set of screen layouts. */
public non-sealed interface SetDesktopSize extends ClientMessage {
    static Builder newBuilder() {
        return new SetDesktopSizeImpl.BuilderImpl();
    }

    int width();
    int height();
    List<Screen> screens();

    interface Builder {
        Builder width(int width);
        Builder height(int height);
        Builder screens(List<Screen> screens);

        SetDesktopSize build();

        default Builder from(SetDesktopSize msg) {
            return width(msg.width()).height(msg.height()).screens(msg.screens());
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static SetDesktopSize read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.SetDesktopSizeImpl.read(in);
    }
}
