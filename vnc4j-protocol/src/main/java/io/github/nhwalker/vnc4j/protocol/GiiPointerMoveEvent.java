package io.github.nhwalker.vnc4j.protocol;

/** A GII pointer movement event (eventType 8=relative, 9=absolute). */
public interface GiiPointerMoveEvent extends GiiEvent {
    int eventType();
    long deviceOrigin();
    int x();
    int y();
    int z();
    int wheel();
}
