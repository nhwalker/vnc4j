package io.github.nhwalker.vnc4j.protocol;

/** Sealed base interface for QEMU audio data messages from server to client. */
public sealed interface QemuAudioServerMessage extends QemuServerMessage
        permits QemuAudioServerBegin, QemuAudioServerEnd, QemuAudioServerData {
}
