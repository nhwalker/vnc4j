package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.PixelFormatImpl;

/** Describes the pixel encoding format used in a VNC session. */
public interface PixelFormat {
    static Builder newBuilder() {
        return new PixelFormatImpl.BuilderImpl();
    }

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

    interface Builder {
        Builder bitsPerPixel(int bitsPerPixel);
        Builder depth(int depth);
        Builder bigEndian(boolean bigEndian);
        Builder trueColour(boolean trueColour);
        Builder redMax(int redMax);
        Builder greenMax(int greenMax);
        Builder blueMax(int blueMax);
        Builder redShift(int redShift);
        Builder greenShift(int greenShift);
        Builder blueShift(int blueShift);

        PixelFormat build();

        default Builder from(PixelFormat msg) {
            return bitsPerPixel(msg.bitsPerPixel()).depth(msg.depth()).bigEndian(msg.bigEndian())
                    .trueColour(msg.trueColour()).redMax(msg.redMax()).greenMax(msg.greenMax())
                    .blueMax(msg.blueMax()).redShift(msg.redShift()).greenShift(msg.greenShift())
                    .blueShift(msg.blueShift());
        }
    }
}
