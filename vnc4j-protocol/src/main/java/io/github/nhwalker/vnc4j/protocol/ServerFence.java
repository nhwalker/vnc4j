package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.ServerFenceImpl;

/** Server fence synchronization message with flags and optional payload. */
public non-sealed interface ServerFence extends ServerMessage {
    static Builder newBuilder() {
        return new ServerFenceImpl.BuilderImpl();
    }

    int flags();
    byte[] payload();

    interface Builder {
        Builder flags(int flags);
        Builder payload(byte[] payload);

        ServerFence build();

        default Builder from(ServerFence msg) {
            return flags(msg.flags()).payload(msg.payload());
        }
    }
}
