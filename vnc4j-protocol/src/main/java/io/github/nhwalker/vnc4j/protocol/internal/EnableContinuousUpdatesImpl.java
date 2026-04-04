package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.EnableContinuousUpdates;

public record EnableContinuousUpdatesImpl(
        boolean enable,
        int x,
        int y,
        int width,
        int height
) implements EnableContinuousUpdates {

    public static final class BuilderImpl implements EnableContinuousUpdates.Builder {
        private boolean enable;
        private int x;
        private int y;
        private int width;
        private int height;

        @Override public Builder enable(boolean v) { this.enable = v; return this; }
        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }

        @Override
        public EnableContinuousUpdates build() {
            return new EnableContinuousUpdatesImpl(enable, x, y, width, height);
        }
    }
}
