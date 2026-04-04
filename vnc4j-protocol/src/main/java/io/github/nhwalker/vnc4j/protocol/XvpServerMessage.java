package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.XvpServerMessageImpl;

/** Server XVP message reporting power management capability or action result. */
public non-sealed interface XvpServerMessage extends ServerMessage {
    static Builder newBuilder() {
        return new XvpServerMessageImpl.BuilderImpl();
    }

    int xvpVersion();
    int xvpMessageCode();

    interface Builder {
        Builder xvpVersion(int xvpVersion);
        Builder xvpMessageCode(int xvpMessageCode);

        XvpServerMessage build();

        default Builder from(XvpServerMessage msg) {
            return xvpVersion(msg.xvpVersion()).xvpMessageCode(msg.xvpMessageCode());
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static XvpServerMessage read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.XvpServerMessageImpl.read(in);
    }
}
