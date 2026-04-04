package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.PointerEvent;

public record PointerEventImpl(int buttonMask, int x, int y) implements PointerEvent {

    public static final class BuilderImpl implements PointerEvent.Builder {
        private int buttonMask;
        private int x;
        private int y;

        @Override public Builder buttonMask(int v) { this.buttonMask = v; return this; }
        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }

        @Override
        public PointerEvent build() {
            return new PointerEventImpl(buttonMask, x, y);
        }
    }
}
