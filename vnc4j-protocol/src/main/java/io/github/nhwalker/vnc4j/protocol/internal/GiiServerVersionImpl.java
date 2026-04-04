package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiServerVersion;

public record GiiServerVersionImpl(boolean bigEndian, int maximumVersion, int minimumVersion) implements GiiServerVersion {

    public static final class BuilderImpl implements GiiServerVersion.Builder {
        private boolean bigEndian;
        private int maximumVersion;
        private int minimumVersion;

        @Override public Builder bigEndian(boolean v) { this.bigEndian = v; return this; }
        @Override public Builder maximumVersion(int v) { this.maximumVersion = v; return this; }
        @Override public Builder minimumVersion(int v) { this.minimumVersion = v; return this; }

        @Override
        public GiiServerVersion build() {
            return new GiiServerVersionImpl(bigEndian, maximumVersion, minimumVersion);
        }
    }
}
