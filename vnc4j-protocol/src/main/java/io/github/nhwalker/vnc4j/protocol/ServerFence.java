package io.github.nhwalker.vnc4j.protocol;

/** Server fence synchronization message with flags and optional payload. */
public non-sealed interface ServerFence extends ServerMessage {
    int flags();
    byte[] payload();
}
