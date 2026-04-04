package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.QemuAudioClientSetFormatImpl;

/** QEMU audio client message to configure the audio sample format, channels, and frequency. */
public non-sealed interface QemuAudioClientSetFormat extends QemuAudioClientMessage {
    static Builder newBuilder() {
        return new QemuAudioClientSetFormatImpl.BuilderImpl();
    }

    int sampleFormat();
    int nchannels();
    long frequency();

    interface Builder {
        Builder sampleFormat(int sampleFormat);
        Builder nchannels(int nchannels);
        Builder frequency(long frequency);

        QemuAudioClientSetFormat build();

        default Builder from(QemuAudioClientSetFormat msg) {
            return sampleFormat(msg.sampleFormat()).nchannels(msg.nchannels()).frequency(msg.frequency());
        }
    }
}
