package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.GiiKeyEventImpl;

/** A GII keyboard press, release, or repeat event (eventType 5=press, 6=release, 7=repeat). */
public non-sealed interface GiiKeyEvent extends GiiEvent {
    static Builder newBuilder() {
        return new GiiKeyEventImpl.BuilderImpl();
    }

    int eventType();
    long deviceOrigin();
    long modifiers();
    long symbol();
    long label();
    long button();

    interface Builder {
        Builder eventType(int eventType);
        Builder deviceOrigin(long deviceOrigin);
        Builder modifiers(long modifiers);
        Builder symbol(long symbol);
        Builder label(long label);
        Builder button(long button);

        GiiKeyEvent build();

        default Builder from(GiiKeyEvent msg) {
            return eventType(msg.eventType()).deviceOrigin(msg.deviceOrigin()).modifiers(msg.modifiers())
                    .symbol(msg.symbol()).label(msg.label()).button(msg.button());
        }
    }

    void write(java.io.OutputStream out, boolean bigEndian) throws java.io.IOException;

    static GiiKeyEvent read(java.io.InputStream in, boolean bigEndian) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.GiiKeyEventImpl.read(in, bigEndian);
    }
}
