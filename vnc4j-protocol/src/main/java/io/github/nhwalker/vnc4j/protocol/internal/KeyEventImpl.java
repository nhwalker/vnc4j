package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.KeyEvent;

public record KeyEventImpl(boolean down, int key) implements KeyEvent {

    public static final class BuilderImpl implements KeyEvent.Builder {
        private boolean down;
        private int key;

        @Override public Builder down(boolean v) { this.down = v; return this; }
        @Override public Builder key(int v) { this.key = v; return this; }

        @Override
        public KeyEvent build() {
            return new KeyEventImpl(down, key);
        }
    }
}
