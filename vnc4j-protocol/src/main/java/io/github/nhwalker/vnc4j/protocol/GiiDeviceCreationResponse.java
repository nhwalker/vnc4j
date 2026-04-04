package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.GiiDeviceCreationResponseImpl;

/** GII server response to a device creation request, carrying the assigned device origin (0=failure). */
public non-sealed interface GiiDeviceCreationResponse extends GiiServerMessage {
    static Builder newBuilder() {
        return new GiiDeviceCreationResponseImpl.BuilderImpl();
    }

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

    void write(java.io.OutputStream out) throws java.io.IOException;

    static GiiDeviceCreationResponse read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.GiiDeviceCreationResponseImpl.read(in);
    }
}
