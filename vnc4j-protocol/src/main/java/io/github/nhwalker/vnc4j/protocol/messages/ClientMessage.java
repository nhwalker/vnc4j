package io.github.nhwalker.vnc4j.protocol.messages;

/** Sealed base interface for all messages sent from client to server. */
public sealed interface ClientMessage extends RfbMessage
        permits SetPixelFormat, SetEncodings, FramebufferUpdateRequest, KeyEvent,
                PointerEvent, ClientCutText {

    void write(java.io.OutputStream out) throws java.io.IOException;
}
