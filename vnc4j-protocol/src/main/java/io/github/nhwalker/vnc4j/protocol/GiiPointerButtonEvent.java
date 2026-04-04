package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.GiiPointerButtonEventImpl;

/** A GII pointer button press or release event (eventType 10=press, 11=release). */
public non-sealed interface GiiPointerButtonEvent extends GiiEvent {
    static Builder newBuilder() {
        return new GiiPointerButtonEventImpl.BuilderImpl();
    }

    int eventType();
    long deviceOrigin();
    long buttonNumber();

    interface Builder {
        Builder eventType(int eventType);
        Builder deviceOrigin(long deviceOrigin);
        Builder buttonNumber(long buttonNumber);

        GiiPointerButtonEvent build();

        default Builder from(GiiPointerButtonEvent msg) {
            return eventType(msg.eventType()).deviceOrigin(msg.deviceOrigin()).buttonNumber(msg.buttonNumber());
        }
    }

    void write(java.io.OutputStream out, boolean bigEndian) throws java.io.IOException;

    static GiiPointerButtonEvent read(java.io.InputStream in, boolean bigEndian) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.GiiPointerButtonEventImpl.read(in, bigEndian);
    }
}
