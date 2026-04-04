package io.github.nhwalker.vnc4j.protocol;

/** Client XVP message for remote power management actions (shutdown, reboot, reset). */
public interface XvpClientMessage extends ClientMessage {
    int xvpVersion();
    int xvpMessageCode();
}
