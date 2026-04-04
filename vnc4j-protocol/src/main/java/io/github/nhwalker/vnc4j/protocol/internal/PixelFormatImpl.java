package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;

public record PixelFormatImpl(
        int bitsPerPixel,
        int depth,
        boolean bigEndian,
        boolean trueColour,
        int redMax,
        int greenMax,
        int blueMax,
        int redShift,
        int greenShift,
        int blueShift
) implements PixelFormat {

    public static final class BuilderImpl implements PixelFormat.Builder {
        private int bitsPerPixel;
        private int depth;
        private boolean bigEndian;
        private boolean trueColour;
        private int redMax;
        private int greenMax;
        private int blueMax;
        private int redShift;
        private int greenShift;
        private int blueShift;

        @Override public Builder bitsPerPixel(int v) { this.bitsPerPixel = v; return this; }
        @Override public Builder depth(int v) { this.depth = v; return this; }
        @Override public Builder bigEndian(boolean v) { this.bigEndian = v; return this; }
        @Override public Builder trueColour(boolean v) { this.trueColour = v; return this; }
        @Override public Builder redMax(int v) { this.redMax = v; return this; }
        @Override public Builder greenMax(int v) { this.greenMax = v; return this; }
        @Override public Builder blueMax(int v) { this.blueMax = v; return this; }
        @Override public Builder redShift(int v) { this.redShift = v; return this; }
        @Override public Builder greenShift(int v) { this.greenShift = v; return this; }
        @Override public Builder blueShift(int v) { this.blueShift = v; return this; }

        @Override
        public PixelFormat build() {
            return new PixelFormatImpl(bitsPerPixel, depth, bigEndian, trueColour,
                    redMax, greenMax, blueMax, redShift, greenShift, blueShift);
        }
    }
}
