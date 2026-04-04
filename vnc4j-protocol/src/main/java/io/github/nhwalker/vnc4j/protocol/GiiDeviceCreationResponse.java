package io.github.nhwalker.vnc4j.protocol;

/** GII server response to a device creation request, carrying the assigned device origin (0=failure). */
public non-sealed interface GiiDeviceCreationResponse extends GiiServerMessage {
    boolean bigEndian();
    long deviceOrigin();

    interface Builder {
        Builder bigEndian(boolean bigEndian);
        Builder deviceOrigin(long deviceOrigin);

        GiiDeviceCreationResponse build();

        default Builder from(GiiDeviceCreationResponse msg) {
            return bigEndian(msg.bigEndian()).deviceOrigin(msg.deviceOrigin());
        }
    }
}
