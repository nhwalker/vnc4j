package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.ClientInitImpl;

/** Client initialisation message indicating whether a shared session is requested. */
public non-sealed interface ClientInit extends RfbMessage {
    static Builder newBuilder() {
        return new ClientInitImpl.BuilderImpl();
    }

    boolean shared();

    interface Builder {
        Builder shared(boolean shared);

        ClientInit build();

        default Builder from(ClientInit msg) {
            return shared(msg.shared());
        }
    }
}
