package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.ClientFenceImpl;

/** Client fence synchronization message with flags and optional payload. */
public non-sealed interface ClientFence extends ClientMessage {
    static Builder newBuilder() {
        return new ClientFenceImpl.BuilderImpl();
    }

    int flags();
    byte[] payload();

    interface Builder {
        Builder flags(int flags);
        Builder payload(byte[] payload);

        ClientFence build();

        default Builder from(ClientFence msg) {
            return flags(msg.flags()).payload(msg.payload());
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static ClientFence read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.ClientFenceImpl.read(in);
    }
}
