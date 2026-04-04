package io.github.nhwalker.vnc4j.protocol;

/** Server fence synchronization message with flags and optional payload. */
public non-sealed interface ServerFence extends ServerMessage {
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
