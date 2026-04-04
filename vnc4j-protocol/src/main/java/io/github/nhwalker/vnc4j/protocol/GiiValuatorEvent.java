package io.github.nhwalker.vnc4j.protocol;

import java.util.List;

/** A GII valuator (axis) change event (eventType 12=relative, 13=absolute). */
public interface GiiValuatorEvent extends GiiEvent {
    int eventType();
    long deviceOrigin();
    long first();
    List<Integer> values();
}
