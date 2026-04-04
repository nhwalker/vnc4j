package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.ClientInit;

public record ClientInitImpl(boolean shared) implements ClientInit {

    public static final class BuilderImpl implements ClientInit.Builder {
        private boolean shared;

        @Override
        public Builder shared(boolean v) {
            this.shared = v;
            return this;
        }

        @Override
        public ClientInit build() {
            return new ClientInitImpl(shared);
        }
    }
}
