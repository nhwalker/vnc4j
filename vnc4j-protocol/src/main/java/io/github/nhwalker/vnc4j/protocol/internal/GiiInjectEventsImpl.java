package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiEvent;
import io.github.nhwalker.vnc4j.protocol.GiiInjectEvents;
import java.util.List;

public record GiiInjectEventsImpl(boolean bigEndian, List<GiiEvent> events) implements GiiInjectEvents {

    public static final class BuilderImpl implements GiiInjectEvents.Builder {
        private boolean bigEndian;
        private List<GiiEvent> events;

        @Override public Builder bigEndian(boolean v) { this.bigEndian = v; return this; }
        @Override public Builder events(List<GiiEvent> v) { this.events = v; return this; }

        @Override
        public GiiInjectEvents build() {
            return new GiiInjectEventsImpl(bigEndian, events);
        }
    }
}
