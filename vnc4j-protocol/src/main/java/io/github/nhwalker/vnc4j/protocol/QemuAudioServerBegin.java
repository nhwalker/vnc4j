package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.QemuAudioServerBeginImpl;

/** QEMU audio server message signalling the beginning of an audio data stream (operation=1). */
public non-sealed interface QemuAudioServerBegin extends QemuAudioServerMessage {
    static Builder newBuilder() {
        return new QemuAudioServerBeginImpl.BuilderImpl();
    }


    interface Builder {
        QemuAudioServerBegin build();

        default Builder from(QemuAudioServerBegin msg) {
            return this;
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static QemuAudioServerBegin read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.QemuAudioServerBeginImpl.read(in);
    }
}
