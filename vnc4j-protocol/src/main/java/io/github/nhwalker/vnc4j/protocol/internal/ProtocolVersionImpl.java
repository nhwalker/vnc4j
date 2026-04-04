package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.ProtocolVersion;

public record ProtocolVersionImpl(int major, int minor) implements ProtocolVersion {

    public static final class BuilderImpl implements ProtocolVersion.Builder {
        private int major;
        private int minor;

        @Override public Builder major(int v) { this.major = v; return this; }
        @Override public Builder minor(int v) { this.minor = v; return this; }

        @Override
        public ProtocolVersion build() {
            return new ProtocolVersionImpl(major, minor);
        }
    }
}
