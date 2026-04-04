package io.github.nhwalker.vnc4j.protocol;

/** Sealed base interface for all messages sent from server to client. */
public sealed interface ServerMessage extends RfbMessage
        permits FramebufferUpdate, SetColourMapEntries, Bell, ServerCutText,
                EndOfContinuousUpdates, ServerFence, XvpServerMessage,
                GiiServerMessage, QemuServerMessage {
}
