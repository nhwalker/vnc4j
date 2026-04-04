package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.QemuAudioClientEnableImpl;

/** QEMU audio client message to enable audio capture (operation=0). */
public non-sealed interface QemuAudioClientEnable extends QemuAudioClientMessage {
    static Builder newBuilder() {
        return new QemuAudioClientEnableImpl.BuilderImpl();
    }


    interface Builder {
        QemuAudioClientEnable build();

        default Builder from(QemuAudioClientEnable msg) {
            return this;
        }
    }
}
