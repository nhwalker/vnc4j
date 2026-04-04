package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiPointerButtonEvent;

public record GiiPointerButtonEventImpl(int eventType, long deviceOrigin, long buttonNumber) implements GiiPointerButtonEvent {

    public static final class BuilderImpl implements GiiPointerButtonEvent.Builder {
        private int eventType;
        private long deviceOrigin;
        private long buttonNumber;

        @Override public Builder eventType(int v) { this.eventType = v; return this; }
        @Override public Builder deviceOrigin(long v) { this.deviceOrigin = v; return this; }
        @Override public Builder buttonNumber(long v) { this.buttonNumber = v; return this; }

        @Override
        public GiiPointerButtonEvent build() {
            return new GiiPointerButtonEventImpl(eventType, deviceOrigin, buttonNumber);
        }
    }
}
