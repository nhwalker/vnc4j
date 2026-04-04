package io.github.nhwalker.vnc4j.protocol;

/** Sealed base interface for QEMU audio control messages from client to server. */
public sealed interface QemuAudioClientMessage extends QemuClientMessage
        permits QemuAudioClientEnable, QemuAudioClientDisable, QemuAudioClientSetFormat {
}
