package io.github.nhwalker.vnc4j.protocol;

/** GII client message requesting destruction of a previously created virtual input device. */
public non-sealed interface GiiDeviceDestruction extends GiiClientMessage {
    boolean bigEndian();
    long deviceOrigin();

    interface Builder {
        Builder bigEndian(boolean bigEndian);
        Builder deviceOrigin(long deviceOrigin);

        GiiDeviceDestruction build();

        default Builder from(GiiDeviceDestruction msg) {
            return bigEndian(msg.bigEndian()).deviceOrigin(msg.deviceOrigin());
        }
    }
}
