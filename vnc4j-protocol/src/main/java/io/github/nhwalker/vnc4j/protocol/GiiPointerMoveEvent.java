package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.GiiPointerMoveEventImpl;

/** A GII pointer movement event (eventType 8=relative, 9=absolute). */
public non-sealed interface GiiPointerMoveEvent extends GiiEvent {
    static Builder newBuilder() {
        return new GiiPointerMoveEventImpl.BuilderImpl();
    }

    int eventType();
    long deviceOrigin();
    int x();
    int y();
    int z();
    int wheel();

    interface Builder {
        Builder eventType(int eventType);
        Builder deviceOrigin(long deviceOrigin);
        Builder x(int x);
        Builder y(int y);
        Builder z(int z);
        Builder wheel(int wheel);

        GiiPointerMoveEvent build();

        default Builder from(GiiPointerMoveEvent msg) {
            return eventType(msg.eventType()).deviceOrigin(msg.deviceOrigin()).x(msg.x()).y(msg.y()).z(msg.z())
                    .wheel(msg.wheel());
        }
    }
}
