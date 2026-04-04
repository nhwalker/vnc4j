package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.SecurityTypeSelection;

public record SecurityTypeSelectionImpl(int securityType) implements SecurityTypeSelection {

    public static final class BuilderImpl implements SecurityTypeSelection.Builder {
        private int securityType;

        @Override
        public Builder securityType(int v) {
            this.securityType = v;
            return this;
        }

        @Override
        public SecurityTypeSelection build() {
            return new SecurityTypeSelectionImpl(securityType);
        }
    }
}
