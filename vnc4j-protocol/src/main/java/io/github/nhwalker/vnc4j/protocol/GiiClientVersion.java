package io.github.nhwalker.vnc4j.protocol;

/** GII client version negotiation message. */
public non-sealed interface GiiClientVersion extends GiiClientMessage {
    boolean bigEndian();
    int version();

    interface Builder {
        Builder bigEndian(boolean bigEndian);
        Builder version(int version);

        GiiClientVersion build();

        default Builder from(GiiClientVersion msg) {
            return bigEndian(msg.bigEndian()).version(msg.version());
        }
    }
}
