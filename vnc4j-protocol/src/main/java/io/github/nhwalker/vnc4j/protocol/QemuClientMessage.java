package io.github.nhwalker.vnc4j.protocol;

/** Sealed base interface for all QEMU sub-messages sent from client to server. */
public sealed interface QemuClientMessage extends ClientMessage
        permits QemuExtendedKeyEvent, QemuAudioClientMessage {
}
