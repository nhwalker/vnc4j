package io.github.nhwalker.vnc4j.protocol;

/** Client pointer (mouse) position and button-state event. */
public interface PointerEvent extends ClientMessage {
    int buttonMask();
    int x();
    int y();
}
