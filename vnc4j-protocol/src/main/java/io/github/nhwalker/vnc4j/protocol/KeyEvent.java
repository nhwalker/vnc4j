package io.github.nhwalker.vnc4j.protocol;

/** Client keyboard press or release event carrying an X11 keysym. */
public non-sealed interface KeyEvent extends ClientMessage {
    boolean down();
    int key();

    interface Builder {
        Builder down(boolean down);
        Builder key(int key);

        KeyEvent build();

        default Builder from(KeyEvent msg) {
            return down(msg.down()).key(msg.key());
        }
    }
}
