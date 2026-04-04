package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.GiiDeviceDestructionImpl;

/** GII client message requesting destruction of a previously created virtual input device. */
public non-sealed interface GiiDeviceDestruction extends GiiClientMessage {
    static Builder newBuilder() {
        return new GiiDeviceDestructionImpl.BuilderImpl();
    }

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

    void write(java.io.OutputStream out) throws java.io.IOException;

    static GiiDeviceDestruction read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.GiiDeviceDestructionImpl.read(in);
    }
}
