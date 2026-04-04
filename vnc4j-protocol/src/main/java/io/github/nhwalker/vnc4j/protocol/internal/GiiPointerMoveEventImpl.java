package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiPointerMoveEvent;

public record GiiPointerMoveEventImpl(
        int eventType,
        long deviceOrigin,
        int x,
        int y,
        int z,
        int wheel
) implements GiiPointerMoveEvent {

    public static final class BuilderImpl implements GiiPointerMoveEvent.Builder {
        private int eventType;
        private long deviceOrigin;
        private int x;
        private int y;
        private int z;
        private int wheel;

        @Override public Builder eventType(int v) { this.eventType = v; return this; }
        @Override public Builder deviceOrigin(long v) { this.deviceOrigin = v; return this; }
        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder z(int v) { this.z = v; return this; }
        @Override public Builder wheel(int v) { this.wheel = v; return this; }

        @Override
        public GiiPointerMoveEvent build() {
            return new GiiPointerMoveEventImpl(eventType, deviceOrigin, x, y, z, wheel);
        }
    }
}
