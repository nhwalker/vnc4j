package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.GiiInjectEventsImpl;

import java.util.List;

/** GII client message injecting a sequence of input events into a virtual device. */
public non-sealed interface GiiInjectEvents extends GiiClientMessage {
    static Builder newBuilder() {
        return new GiiInjectEventsImpl.BuilderImpl();
    }

    boolean bigEndian();
    List<GiiEvent> events();

    interface Builder {
        Builder bigEndian(boolean bigEndian);
        Builder events(List<GiiEvent> events);

        GiiInjectEvents build();

        default Builder from(GiiInjectEvents msg) {
            return bigEndian(msg.bigEndian()).events(msg.events());
        }
    }
}
