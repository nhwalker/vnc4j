package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.QemuExtendedKeyEventImpl;

/** QEMU extended key event carrying an X11 keysym and an XT scancode. */
public non-sealed interface QemuExtendedKeyEvent extends QemuClientMessage {
    static Builder newBuilder() {
        return new QemuExtendedKeyEventImpl.BuilderImpl();
    }

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

    void write(java.io.OutputStream out) throws java.io.IOException;

    static QemuExtendedKeyEvent read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.QemuExtendedKeyEventImpl.read(in);
    }
}
