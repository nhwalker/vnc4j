package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiDeviceDestruction;

public record GiiDeviceDestructionImpl(boolean bigEndian, long deviceOrigin) implements GiiDeviceDestruction {

    public static final class BuilderImpl implements GiiDeviceDestruction.Builder {
        private boolean bigEndian;
        private long deviceOrigin;

        @Override public Builder bigEndian(boolean v) { this.bigEndian = v; return this; }
        @Override public Builder deviceOrigin(long v) { this.deviceOrigin = v; return this; }

        @Override
        public GiiDeviceDestruction build() {
            return new GiiDeviceDestructionImpl(bigEndian, deviceOrigin);
        }
    }
}
