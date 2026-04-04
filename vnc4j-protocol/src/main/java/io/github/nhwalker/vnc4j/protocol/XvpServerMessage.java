package io.github.nhwalker.vnc4j.protocol;

/** Server XVP message reporting power management capability or action result. */
public non-sealed interface XvpServerMessage extends ServerMessage {
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
}
