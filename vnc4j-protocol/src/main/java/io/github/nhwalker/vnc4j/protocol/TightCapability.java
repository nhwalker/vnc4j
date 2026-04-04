package io.github.nhwalker.vnc4j.protocol;

/** Describes a capability advertised via the Tight security extension. */
public interface TightCapability {
    int code();
    String vendor();
    String signature();

    interface Builder {
        Builder code(int code);
        Builder vendor(String vendor);
        Builder signature(String signature);

        TightCapability build();

        default Builder from(TightCapability msg) {
            return code(msg.code()).vendor(msg.vendor()).signature(msg.signature());
        }
    }
}
