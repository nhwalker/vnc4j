package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiDeviceCreationResponse;

public record GiiDeviceCreationResponseImpl(boolean bigEndian, long deviceOrigin) implements GiiDeviceCreationResponse {

    public static final class BuilderImpl implements GiiDeviceCreationResponse.Builder {
        private boolean bigEndian;
        private long deviceOrigin;

        @Override public Builder bigEndian(boolean v) { this.bigEndian = v; return this; }
        @Override public Builder deviceOrigin(long v) { this.deviceOrigin = v; return this; }

        @Override
        public GiiDeviceCreationResponse build() {
            return new GiiDeviceCreationResponseImpl(bigEndian, deviceOrigin);
        }
    }
}
