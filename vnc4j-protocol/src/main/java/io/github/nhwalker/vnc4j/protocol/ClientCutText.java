package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.ClientCutTextImpl;

/** Client clipboard (cut-text) message carrying ISO 8859-1 encoded text. */
public non-sealed interface ClientCutText extends ClientMessage {
    static Builder newBuilder() {
        return new ClientCutTextImpl.BuilderImpl();
    }

    byte[] text();

    interface Builder {
        Builder text(byte[] text);

        ClientCutText build();

        default Builder from(ClientCutText msg) {
            return text(msg.text());
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static ClientCutText read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.ClientCutTextImpl.read(in);
    }
}
