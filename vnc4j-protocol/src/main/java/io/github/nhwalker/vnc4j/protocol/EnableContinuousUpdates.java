package io.github.nhwalker.vnc4j.protocol;

/** Client message to enable or disable continuous framebuffer updates for a region. */
public non-sealed interface EnableContinuousUpdates extends ClientMessage {
    boolean enable();
    int x();
    int y();
    int width();
    int height();
}
