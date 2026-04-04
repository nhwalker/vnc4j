package io.github.nhwalker.vnc4j.protocol;

/** Server message instructing the client to ring an audible or visual bell. */
public non-sealed interface Bell extends ServerMessage {

    interface Builder {
        Bell build();

        default Builder from(Bell msg) {
            return this;
        }
    }
}
