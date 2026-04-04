package io.github.nhwalker.vnc4j.protocol;

/** Server XVP message reporting power management capability or action result. */
public non-sealed interface XvpServerMessage extends ServerMessage {
    int xvpVersion();
    int xvpMessageCode();
}
