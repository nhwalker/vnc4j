package io.github.nhwalker.vnc4j.protocol;

/** Client clipboard (cut-text) message carrying ISO 8859-1 encoded text. */
public interface ClientCutText extends ClientMessage {
    byte[] text();
}
