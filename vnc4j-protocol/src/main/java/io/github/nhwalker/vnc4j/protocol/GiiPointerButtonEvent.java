package io.github.nhwalker.vnc4j.protocol;

/** A GII pointer button press or release event (eventType 10=press, 11=release). */
public interface GiiPointerButtonEvent extends GiiEvent {
    int eventType();
    long deviceOrigin();
    long buttonNumber();
}
