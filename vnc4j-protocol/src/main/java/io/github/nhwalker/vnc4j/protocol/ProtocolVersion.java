package io.github.nhwalker.vnc4j.protocol;

/** RFB protocol version handshake message. */
public interface ProtocolVersion extends RfbMessage {
    int major();
    int minor();
}
