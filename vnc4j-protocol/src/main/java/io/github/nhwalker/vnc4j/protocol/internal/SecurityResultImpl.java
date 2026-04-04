package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.SecurityResult;

public record SecurityResultImpl(int status, String failureReason) implements SecurityResult {

    public static final class BuilderImpl implements SecurityResult.Builder {
        private int status;
        private String failureReason;

        @Override public Builder status(int v) { this.status = v; return this; }
        @Override public Builder failureReason(String v) { this.failureReason = v; return this; }

        @Override
        public SecurityResult build() {
            return new SecurityResultImpl(status, failureReason);
        }
    }
}
