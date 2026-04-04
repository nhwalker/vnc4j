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

    void write(java.io.OutputStream out) throws java.io.IOException;

    static QemuAudioServerEnd read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.QemuAudioServerEndImpl.read(in);
    }
}
