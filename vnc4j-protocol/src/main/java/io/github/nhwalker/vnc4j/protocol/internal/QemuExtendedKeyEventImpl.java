package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.QemuExtendedKeyEvent;

public record QemuExtendedKeyEventImpl(int downFlag, int keysym, int keycode) implements QemuExtendedKeyEvent {

    public static final class BuilderImpl implements QemuExtendedKeyEvent.Builder {
        private int downFlag;
        private int keysym;
        private int keycode;

        @Override public Builder downFlag(int v) { this.downFlag = v; return this; }
        @Override public Builder keysym(int v) { this.keysym = v; return this; }
        @Override public Builder keycode(int v) { this.keycode = v; return this; }

        @Override
        public QemuExtendedKeyEvent build() {
            return new QemuExtendedKeyEventImpl(downFlag, keysym, keycode);
        }
    }
}
