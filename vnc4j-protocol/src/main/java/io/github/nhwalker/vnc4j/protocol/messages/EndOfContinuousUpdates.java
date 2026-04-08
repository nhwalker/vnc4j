package io.github.nhwalker.vnc4j.protocol.messages;

import io.github.nhwalker.vnc4j.protocol.internal.EndOfContinuousUpdatesImpl;

/** Server message signalling the end of continuous update mode. */
public non-sealed interface EndOfContinuousUpdates extends ServerMessage {
    static Builder newBuilder() {
        return new EndOfContinuousUpdatesImpl.BuilderImpl();
    }


    interface Builder {
        EndOfContinuousUpdates build();

        default Builder from(EndOfContinuousUpdates msg) {
            return this;
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static EndOfContinuousUpdates read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.EndOfContinuousUpdatesImpl.read(in);
    }
}
