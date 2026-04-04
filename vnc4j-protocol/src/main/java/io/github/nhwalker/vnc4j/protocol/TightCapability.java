package io.github.nhwalker.vnc4j.protocol;

/** Describes a capability advertised via the Tight security extension. */
public interface TightCapability {
    int code();
    String vendor();
    String signature();
}
