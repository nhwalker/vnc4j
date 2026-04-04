package io.github.nhwalker.vnc4j.protocol;

/** Describes the pixel encoding format used in a VNC session. */
public interface PixelFormat {
    int bitsPerPixel();
    int depth();
    boolean bigEndian();
    boolean trueColour();
    int redMax();
    int greenMax();
    int blueMax();
    int redShift();
    int greenShift();
    int blueShift();
}
