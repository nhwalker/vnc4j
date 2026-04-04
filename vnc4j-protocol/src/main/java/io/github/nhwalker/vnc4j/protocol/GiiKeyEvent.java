package io.github.nhwalker.vnc4j.protocol;

/** A GII keyboard press, release, or repeat event (eventType 5=press, 6=release, 7=repeat). */
public interface GiiKeyEvent extends GiiEvent {
    int eventType();
    long deviceOrigin();
    long modifiers();
    long symbol();
    long label();
    long button();
}
