package io.github.nhwalker.vnc4j.protocol;

/** Sealed base interface for all GII sub-messages sent from server to client. */
public sealed interface GiiServerMessage extends ServerMessage
        permits GiiServerVersion, GiiDeviceCreationResponse {
}
