package io.github.nhwalker.vnc4j.protocol;

/** Sealed base interface for all QEMU sub-messages sent from server to client. */
public sealed interface QemuServerMessage extends ServerMessage
        permits QemuAudioServerMessage {
}
