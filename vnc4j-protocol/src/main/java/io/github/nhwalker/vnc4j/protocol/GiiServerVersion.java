package io.github.nhwalker.vnc4j.protocol;

/** GII server version negotiation response advertising the supported version range. */
public non-sealed interface GiiServerVersion extends GiiServerMessage {
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
}
