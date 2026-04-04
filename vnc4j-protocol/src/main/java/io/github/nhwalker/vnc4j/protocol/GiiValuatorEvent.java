package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.GiiValuatorEventImpl;

import java.util.List;

/** A GII valuator (axis) change event (eventType 12=relative, 13=absolute). */
public non-sealed interface GiiValuatorEvent extends GiiEvent {
    static Builder newBuilder() {
        return new GiiValuatorEventImpl.BuilderImpl();
    }

    int eventType();
    long deviceOrigin();
    long first();
    List<Integer> values();

    interface Builder {
        Builder eventType(int eventType);
        Builder deviceOrigin(long deviceOrigin);
        Builder first(long first);
        Builder values(List<Integer> values);

        GiiValuatorEvent build();

        default Builder from(GiiValuatorEvent msg) {
            return eventType(msg.eventType()).deviceOrigin(msg.deviceOrigin()).first(msg.first())
                    .values(msg.values());
        }
    }
}
