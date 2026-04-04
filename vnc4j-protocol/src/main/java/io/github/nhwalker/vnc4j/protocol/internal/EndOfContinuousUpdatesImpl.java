package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.EndOfContinuousUpdates;

public final class EndOfContinuousUpdatesImpl implements EndOfContinuousUpdates {

    public static final EndOfContinuousUpdatesImpl INSTANCE = new EndOfContinuousUpdatesImpl();

    private EndOfContinuousUpdatesImpl() {}

    @Override
    public boolean equals(Object o) {
        return o instanceof EndOfContinuousUpdates;
    }

    @Override
    public int hashCode() {
        return EndOfContinuousUpdates.class.hashCode();
    }

    @Override
    public String toString() {
        return "EndOfContinuousUpdates[]";
    }

    public static final class BuilderImpl implements EndOfContinuousUpdates.Builder {
        @Override
        public EndOfContinuousUpdates build() {
            return INSTANCE;
        }
    }
}
