package io.github.nhwalker.vnc4j.protocol;

/** Client pointer (mouse) position and button-state event. */
public non-sealed interface PointerEvent extends ClientMessage {
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
}
