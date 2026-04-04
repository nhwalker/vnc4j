package io.github.nhwalker.vnc4j.protocol;

/** Represents a single encoded rectangle within a FramebufferUpdate. */
public interface RfbRectangle {
    int x();
    int y();
    int width();
    int height();
    int encodingType();
    byte[] data();
}
