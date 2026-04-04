package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.ColourMapEntry;

public record ColourMapEntryImpl(int red, int green, int blue) implements ColourMapEntry {

    public static final class BuilderImpl implements ColourMapEntry.Builder {
        private int red;
        private int green;
        private int blue;

        @Override public Builder red(int v) { this.red = v; return this; }
        @Override public Builder green(int v) { this.green = v; return this; }
        @Override public Builder blue(int v) { this.blue = v; return this; }

        @Override
        public ColourMapEntry build() {
            return new ColourMapEntryImpl(red, green, blue);
        }
    }
}
