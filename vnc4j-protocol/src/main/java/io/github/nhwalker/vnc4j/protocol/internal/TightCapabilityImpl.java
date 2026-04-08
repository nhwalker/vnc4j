package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.messages.TightCapability;

public record TightCapabilityImpl(int code, String vendor, String signature) implements TightCapability {

    public static final class BuilderImpl implements TightCapability.Builder {
        private int code;
        private String vendor;
        private String signature;

        @Override public Builder code(int v) { this.code = v; return this; }
        @Override public Builder vendor(String v) { this.vendor = v; return this; }
        @Override public Builder signature(String v) { this.signature = v; return this; }

        @Override
        public TightCapability build() {
            return new TightCapabilityImpl(code, vendor, signature);
        }
    }
}
