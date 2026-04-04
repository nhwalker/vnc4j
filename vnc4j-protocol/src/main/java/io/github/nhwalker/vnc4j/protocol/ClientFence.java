package io.github.nhwalker.vnc4j.protocol;

/** Client fence synchronization message with flags and optional payload. */
public interface ClientFence extends ClientMessage {
    int flags();
    byte[] payload();
}
