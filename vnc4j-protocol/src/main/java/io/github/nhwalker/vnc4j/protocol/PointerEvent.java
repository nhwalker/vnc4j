package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.PointerEventImpl;

/** Client pointer (mouse) position and button-state event. */
public non-sealed interface PointerEvent extends ClientMessage {
    static Builder newBuilder() {
        return new PointerEventImpl.BuilderImpl();
    }

    int buttonMask();
    int x();
    int y();

    interface Builder {
        Builder buttonMask(int buttonMask);
        Builder x(int x);
        Builder y(int y);

        PointerEvent build();

        default Builder from(PointerEvent msg) {
            return buttonMask(msg.buttonMask()).x(msg.x()).y(msg.y());
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static PointerEvent read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.PointerEventImpl.read(in);
    }
}
