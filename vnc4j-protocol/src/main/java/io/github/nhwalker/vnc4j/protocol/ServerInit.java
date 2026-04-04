package io.github.nhwalker.vnc4j.protocol;

/** Server initialisation message describing the framebuffer and desktop name. */
public interface ServerInit extends RfbMessage {
    int framebufferWidth();
    int framebufferHeight();
    PixelFormat pixelFormat();
    String name();
}
