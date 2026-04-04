package io.github.nhwalker.vnc4j.protocol;

/** Sealed base interface for all GII input events that can be injected. */
public sealed interface GiiEvent
        permits GiiKeyEvent, GiiPointerMoveEvent, GiiPointerButtonEvent, GiiValuatorEvent {
    int eventType();
}
