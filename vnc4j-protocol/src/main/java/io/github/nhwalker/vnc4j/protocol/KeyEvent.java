package io.github.nhwalker.vnc4j.protocol;

/** Client keyboard press or release event carrying an X11 keysym. */
public interface KeyEvent extends ClientMessage {
    boolean down();
    int key();
}
