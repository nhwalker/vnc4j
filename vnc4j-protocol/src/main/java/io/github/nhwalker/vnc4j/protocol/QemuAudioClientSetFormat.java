package io.github.nhwalker.vnc4j.protocol;

/** QEMU audio client message to configure the audio sample format, channels, and frequency. */
public non-sealed interface QemuAudioClientSetFormat extends QemuAudioClientMessage {
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
