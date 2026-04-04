package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiClientVersion;

public record GiiClientVersionImpl(boolean bigEndian, int version) implements GiiClientVersion {

    public static final class BuilderImpl implements GiiClientVersion.Builder {
        private boolean bigEndian;
        private int version;

        @Override public Builder bigEndian(boolean v) { this.bigEndian = v; return this; }
        @Override public Builder version(int v) { this.version = v; return this; }

        @Override
        public GiiClientVersion build() {
            return new GiiClientVersionImpl(bigEndian, version);
        }
    }
}
