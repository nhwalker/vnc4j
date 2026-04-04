package io.github.nhwalker.vnc4j.protocol;

/** GII server response to a device creation request, carrying the assigned device origin (0=failure). */
public non-sealed interface GiiDeviceCreationResponse extends GiiServerMessage {
    boolean bigEndian();
    long deviceOrigin();
}
