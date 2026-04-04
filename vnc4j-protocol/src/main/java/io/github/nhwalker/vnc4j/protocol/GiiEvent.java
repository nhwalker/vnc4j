package io.github.nhwalker.vnc4j.protocol;

/** Sealed base interface for all GII input events that can be injected. */
public sealed interface GiiEvent
        permits GiiKeyEvent, GiiPointerMoveEvent, GiiPointerButtonEvent, GiiValuatorEvent {
    int eventType();

    void write(java.io.OutputStream out, boolean bigEndian) throws java.io.IOException;

    static GiiEvent readEvent(java.io.InputStream in, boolean bigEndian) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.GiiEventDispatch.readEvent(in, bigEndian);
    }
}
