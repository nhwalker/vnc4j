package io.github.nhwalker.vnc4j.protocol;

/** QEMU extended key event carrying an X11 keysym and an XT scancode. */
public non-sealed interface QemuExtendedKeyEvent extends QemuClientMessage {
    int downFlag();
    int keysym();
    int keycode();

    interface Builder {
        Builder downFlag(int downFlag);
        Builder keysym(int keysym);
        Builder keycode(int keycode);

        QemuExtendedKeyEvent build();

        default Builder from(QemuExtendedKeyEvent msg) {
            return downFlag(msg.downFlag()).keysym(msg.keysym()).keycode(msg.keycode());
        }
    }
}
