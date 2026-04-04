package io.github.nhwalker.vnc4j.protocol;

/** GII server version negotiation response advertising the supported version range. */
public non-sealed interface GiiServerVersion extends GiiServerMessage {
    boolean bigEndian();
    int maximumVersion();
    int minimumVersion();
}
