package io.github.nhwalker.vnc4j.protocol;

/** GII client version negotiation message. */
public non-sealed interface GiiClientVersion extends GiiClientMessage {
    boolean bigEndian();
    int version();
}
