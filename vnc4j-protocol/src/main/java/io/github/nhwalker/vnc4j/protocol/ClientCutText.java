package io.github.nhwalker.vnc4j.protocol;

/** Client clipboard (cut-text) message carrying ISO 8859-1 encoded text. */
public non-sealed interface ClientCutText extends ClientMessage {
    byte[] text();

    interface Builder {
        Builder text(byte[] text);

        ClientCutText build();

        default Builder from(ClientCutText msg) {
            return text(msg.text());
        }
    }
}
