package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.ProtocolVersionImpl;

/** RFB protocol version handshake message. */
public non-sealed interface ProtocolVersion extends RfbMessage {
    static Builder newBuilder() {
        return new ProtocolVersionImpl.BuilderImpl();
    }

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

    void write(java.io.OutputStream out) throws java.io.IOException;

    static ProtocolVersion read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.ProtocolVersionImpl.read(in);
    }
}
