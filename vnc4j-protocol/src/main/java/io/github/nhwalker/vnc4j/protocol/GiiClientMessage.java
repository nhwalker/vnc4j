package io.github.nhwalker.vnc4j.protocol;

/** Sealed base interface for all GII sub-messages sent from client to server. */
public sealed interface GiiClientMessage extends ClientMessage
        permits GiiClientVersion, GiiDeviceCreation, GiiDeviceDestruction, GiiInjectEvents {
    boolean bigEndian();
}
