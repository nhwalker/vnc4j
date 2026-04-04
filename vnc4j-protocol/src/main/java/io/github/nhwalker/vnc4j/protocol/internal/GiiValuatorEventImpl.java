package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiValuatorEvent;
import java.util.List;

public record GiiValuatorEventImpl(int eventType, long deviceOrigin, long first, List<Integer> values) implements GiiValuatorEvent {

    public static final class BuilderImpl implements GiiValuatorEvent.Builder {
        private int eventType;
        private long deviceOrigin;
        private long first;
        private List<Integer> values;

        @Override public Builder eventType(int v) { this.eventType = v; return this; }
        @Override public Builder deviceOrigin(long v) { this.deviceOrigin = v; return this; }
        @Override public Builder first(long v) { this.first = v; return this; }
        @Override public Builder values(List<Integer> v) { this.values = v; return this; }

        @Override
        public GiiValuatorEvent build() {
            return new GiiValuatorEventImpl(eventType, deviceOrigin, first, values);
        }
    }
}
