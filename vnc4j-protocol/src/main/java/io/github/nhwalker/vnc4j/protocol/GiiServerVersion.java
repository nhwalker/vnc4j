package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.GiiServerVersionImpl;

/** GII server version negotiation response advertising the supported version range. */
public non-sealed interface GiiServerVersion extends GiiServerMessage {
    static Builder newBuilder() {
        return new GiiServerVersionImpl.BuilderImpl();
    }

    boolean bigEndian();
    int maximumVersion();
    int minimumVersion();

    interface Builder {
        Builder bigEndian(boolean bigEndian);
        Builder maximumVersion(int maximumVersion);
        Builder minimumVersion(int minimumVersion);

        GiiServerVersion build();

        default Builder from(GiiServerVersion msg) {
            return bigEndian(msg.bigEndian()).maximumVersion(msg.maximumVersion()).minimumVersion(msg.minimumVersion());
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static GiiServerVersion read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.GiiServerVersionImpl.read(in);
    }
}
