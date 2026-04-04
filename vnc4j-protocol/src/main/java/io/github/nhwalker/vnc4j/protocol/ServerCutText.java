package io.github.nhwalker.vnc4j.protocol;

/** Server clipboard (cut-text) message carrying ISO 8859-1 encoded text. */
public non-sealed interface ServerCutText extends ServerMessage {
    byte[] text();

    interface Builder {
        Builder text(byte[] text);

        ServerCutText build();

        default Builder from(ServerCutText msg) {
            return text(msg.text());
        }
    }
}
