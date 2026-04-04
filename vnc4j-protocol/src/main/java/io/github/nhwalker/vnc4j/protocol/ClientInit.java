package io.github.nhwalker.vnc4j.protocol;

/** Client initialisation message indicating whether a shared session is requested. */
public non-sealed interface ClientInit extends RfbMessage {
    boolean shared();

    interface Builder {
        Builder shared(boolean shared);

        ClientInit build();

        default Builder from(ClientInit msg) {
            return shared(msg.shared());
        }
    }
}
