package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.XvpClientMessage;

public record XvpClientMessageImpl(int xvpVersion, int xvpMessageCode) implements XvpClientMessage {

    public static final class BuilderImpl implements XvpClientMessage.Builder {
        private int xvpVersion;
        private int xvpMessageCode;

        @Override public Builder xvpVersion(int v) { this.xvpVersion = v; return this; }
        @Override public Builder xvpMessageCode(int v) { this.xvpMessageCode = v; return this; }

        @Override
        public XvpClientMessage build() {
            return new XvpClientMessageImpl(xvpVersion, xvpMessageCode);
        }
    }
}
