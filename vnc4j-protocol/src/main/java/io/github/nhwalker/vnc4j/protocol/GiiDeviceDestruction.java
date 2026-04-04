package io.github.nhwalker.vnc4j.protocol;

/** GII client message requesting destruction of a previously created virtual input device. */
public non-sealed interface GiiDeviceDestruction extends GiiClientMessage {
    boolean bigEndian();
    long deviceOrigin();
}
