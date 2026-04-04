package io.github.nhwalker.vnc4j.protocol;

/** A GII keyboard press, release, or repeat event (eventType 5=press, 6=release, 7=repeat). */
public non-sealed interface GiiKeyEvent extends GiiEvent {
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
}
