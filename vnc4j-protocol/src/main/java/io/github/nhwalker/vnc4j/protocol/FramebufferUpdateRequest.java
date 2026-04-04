package io.github.nhwalker.vnc4j.protocol;

/** Client request for a framebuffer update for the specified region. */
public non-sealed interface FramebufferUpdateRequest extends ClientMessage {
    boolean incremental();
    int x();
    int y();
    int width();
    int height();
}
