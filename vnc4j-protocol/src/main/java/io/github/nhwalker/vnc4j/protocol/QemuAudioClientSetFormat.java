package io.github.nhwalker.vnc4j.protocol;

/** QEMU audio client message to configure the audio sample format, channels, and frequency. */
public non-sealed interface QemuAudioClientSetFormat extends QemuAudioClientMessage {
    int sampleFormat();
    int nchannels();
    long frequency();
}
