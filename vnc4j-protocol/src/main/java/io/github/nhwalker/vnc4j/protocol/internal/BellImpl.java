package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.Bell;

public final class BellImpl implements Bell {

    public static final BellImpl INSTANCE = new BellImpl();

    private BellImpl() {}

    @Override
    public boolean equals(Object o) {
        return o instanceof Bell;
    }

    @Override
    public int hashCode() {
        return Bell.class.hashCode();
    }

    @Override
    public String toString() {
        return "Bell[]";
    }

    public static final class BuilderImpl implements Bell.Builder {
        @Override
        public Bell build() {
            return INSTANCE;
        }
    }
}
