package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.XvpServerMessage;

public record XvpServerMessageImpl(int xvpVersion, int xvpMessageCode) implements XvpServerMessage {

    public static final class BuilderImpl implements XvpServerMessage.Builder {
        private int xvpVersion;
        private int xvpMessageCode;

        @Override public Builder xvpVersion(int v) { this.xvpVersion = v; return this; }
        @Override public Builder xvpMessageCode(int v) { this.xvpMessageCode = v; return this; }

        @Override
        public XvpServerMessage build() {
            return new XvpServerMessageImpl(xvpVersion, xvpMessageCode);
        }
    }
}
