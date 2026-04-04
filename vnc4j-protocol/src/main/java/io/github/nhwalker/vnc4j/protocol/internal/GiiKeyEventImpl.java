package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiKeyEvent;

public record GiiKeyEventImpl(
        int eventType,
        long deviceOrigin,
        long modifiers,
        long symbol,
        long label,
        long button
) implements GiiKeyEvent {

    public static final class BuilderImpl implements GiiKeyEvent.Builder {
        private int eventType;
        private long deviceOrigin;
        private long modifiers;
        private long symbol;
        private long label;
        private long button;

        @Override public Builder eventType(int v) { this.eventType = v; return this; }
        @Override public Builder deviceOrigin(long v) { this.deviceOrigin = v; return this; }
        @Override public Builder modifiers(long v) { this.modifiers = v; return this; }
        @Override public Builder symbol(long v) { this.symbol = v; return this; }
        @Override public Builder label(long v) { this.label = v; return this; }
        @Override public Builder button(long v) { this.button = v; return this; }

        @Override
        public GiiKeyEvent build() {
            return new GiiKeyEventImpl(eventType, deviceOrigin, modifiers, symbol, label, button);
        }
    }
}
