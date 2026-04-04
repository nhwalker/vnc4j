package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.XvpClientMessageImpl;

/** Client XVP message for remote power management actions (shutdown, reboot, reset). */
public non-sealed interface XvpClientMessage extends ClientMessage {
    static Builder newBuilder() {
        return new XvpClientMessageImpl.BuilderImpl();
    }

    int xvpVersion();
    int xvpMessageCode();

    interface Builder {
        Builder xvpVersion(int xvpVersion);
        Builder xvpMessageCode(int xvpMessageCode);

        XvpClientMessage build();

        default Builder from(XvpClientMessage msg) {
            return xvpVersion(msg.xvpVersion()).xvpMessageCode(msg.xvpMessageCode());
        }
    }
}
