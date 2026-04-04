package io.github.nhwalker.vnc4j.protocol;

/** QEMU extended key event carrying an X11 keysym and an XT scancode. */
public non-sealed interface QemuExtendedKeyEvent extends QemuClientMessage {
    int downFlag();
    int keysym();
    int keycode();
}
