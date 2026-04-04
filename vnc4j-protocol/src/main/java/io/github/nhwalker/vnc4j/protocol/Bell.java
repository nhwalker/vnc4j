package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.BellImpl;

/** Server message instructing the client to ring an audible or visual bell. */
public non-sealed interface Bell extends ServerMessage {
    static Builder newBuilder() {
        return new BellImpl.BuilderImpl();
    }


    interface Builder {
        Bell build();

        default Builder from(Bell msg) {
            return this;
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static Bell read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.BellImpl.read(in);
    }
}
