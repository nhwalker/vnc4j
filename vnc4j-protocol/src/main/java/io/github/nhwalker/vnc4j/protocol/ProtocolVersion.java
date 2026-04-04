package io.github.nhwalker.vnc4j.protocol;

/** RFB protocol version handshake message. */
public non-sealed interface ProtocolVersion extends RfbMessage {
    int major();
    int minor();

    interface Builder {
        Builder major(int major);
        Builder minor(int minor);

        ProtocolVersion build();

        default Builder from(ProtocolVersion msg) {
            return major(msg.major()).minor(msg.minor());
        }
    }
}
