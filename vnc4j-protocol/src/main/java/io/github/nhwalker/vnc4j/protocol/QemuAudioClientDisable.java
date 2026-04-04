package io.github.nhwalker.vnc4j.protocol;

/** QEMU audio client message to disable audio capture (operation=1). */
public non-sealed interface QemuAudioClientDisable extends QemuAudioClientMessage {

    interface Builder {
        QemuAudioClientDisable build();

        default Builder from(QemuAudioClientDisable msg) {
            return this;
        }
    }
}
