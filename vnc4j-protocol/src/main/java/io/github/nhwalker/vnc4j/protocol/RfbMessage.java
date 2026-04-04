package io.github.nhwalker.vnc4j.protocol;

/** Sealed root interface for all RFB protocol messages. */
public sealed interface RfbMessage
        permits ProtocolVersion, SecurityTypes, SecurityTypeSelection, SecurityResult,
                ClientInit, ServerInit, ClientMessage, ServerMessage {
}
