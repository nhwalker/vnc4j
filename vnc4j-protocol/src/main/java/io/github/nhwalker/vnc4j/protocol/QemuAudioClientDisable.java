package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.QemuAudioClientDisableImpl;

/** QEMU audio client message to disable audio capture (operation=1). */
public non-sealed interface QemuAudioClientDisable extends QemuAudioClientMessage {
    static Builder newBuilder() {
        return new QemuAudioClientDisableImpl.BuilderImpl();
    }


    interface Builder {
        QemuAudioClientDisable build();

        default Builder from(QemuAudioClientDisable msg) {
            return this;
        }
    }
}
