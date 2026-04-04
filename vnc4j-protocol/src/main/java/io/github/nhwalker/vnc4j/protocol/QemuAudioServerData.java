package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.QemuAudioServerDataImpl;

/** QEMU audio server message carrying a chunk of raw audio sample data. */
public non-sealed interface QemuAudioServerData extends QemuAudioServerMessage {
    static Builder newBuilder() {
        return new QemuAudioServerDataImpl.BuilderImpl();
    }

    byte[] data();

    interface Builder {
        Builder data(byte[] data);

        QemuAudioServerData build();

        default Builder from(QemuAudioServerData msg) {
            return data(msg.data());
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static QemuAudioServerData read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.QemuAudioServerDataImpl.read(in);
    }
}
