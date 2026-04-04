package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.QemuAudioServerEndImpl;

/** QEMU audio server message signalling the end of an audio data stream (operation=0). */
public non-sealed interface QemuAudioServerEnd extends QemuAudioServerMessage {
    static Builder newBuilder() {
        return new QemuAudioServerEndImpl.BuilderImpl();
    }


    interface Builder {
        QemuAudioServerEnd build();

        default Builder from(QemuAudioServerEnd msg) {
            return this;
        }
    }
}
