package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.Screen;

public record ScreenImpl(long id, int x, int y, int width, int height, long flags) implements Screen {

    public static final class BuilderImpl implements Screen.Builder {
        private long id;
        private int x;
        private int y;
        private int width;
        private int height;
        private long flags;

        @Override public Builder id(long v) { this.id = v; return this; }
        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }
        @Override public Builder flags(long v) { this.flags = v; return this; }

        @Override
        public Screen build() {
            return new ScreenImpl(id, x, y, width, height, flags);
        }
    }
}
