package io.github.nhwalker.vnc4j.protocol;

/** Client fence synchronization message with flags and optional payload. */
public non-sealed interface ClientFence extends ClientMessage {
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
}
