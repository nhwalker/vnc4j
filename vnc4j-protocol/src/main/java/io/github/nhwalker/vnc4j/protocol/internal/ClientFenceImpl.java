package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.ClientFence;
import java.util.Arrays;
import java.util.Objects;

public final class ClientFenceImpl implements ClientFence {
    private final int flags;
    private final byte[] payload;

    public ClientFenceImpl(int flags, byte[] payload) {
        this.flags = flags;
        this.payload = payload;
    }

    @Override
    public int flags() {
        return flags;
    }

    @Override
    public byte[] payload() {
        return payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientFence other)) return false;
        return flags == other.flags() && Arrays.equals(payload, other.payload());
    }

    @Override
    public int hashCode() {
        return Objects.hash(flags, Arrays.hashCode(payload));
    }

    @Override
    public String toString() {
        return "ClientFence[flags=" + flags + ", payload=" + Arrays.toString(payload) + "]";
    }

    public static final class BuilderImpl implements ClientFence.Builder {
        private int flags;
        private byte[] payload;

        @Override
        public Builder flags(int flags) {
            this.flags = flags;
            return this;
        }

        @Override
        public Builder payload(byte[] payload) {
            this.payload = payload;
            return this;
        }

        @Override
        public ClientFence build() {
            return new ClientFenceImpl(flags, payload);
        }
    }
}
