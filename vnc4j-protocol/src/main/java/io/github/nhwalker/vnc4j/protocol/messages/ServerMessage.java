package io.github.nhwalker.vnc4j.protocol.messages;

/** Sealed base interface for all messages sent from server to client. */
public sealed interface ServerMessage extends RfbMessage
        permits FramebufferUpdate, SetColourMapEntries, Bell, ServerCutText,
                EndOfContinuousUpdates, ServerFence, XvpServerMessage {

    void write(java.io.OutputStream out) throws java.io.IOException;
}
