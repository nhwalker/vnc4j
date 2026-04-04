package io.github.nhwalker.vnc4j.protocol;

/** QEMU audio server message carrying a chunk of raw audio sample data. */
public non-sealed interface QemuAudioServerData extends QemuAudioServerMessage {
    byte[] data();
}
