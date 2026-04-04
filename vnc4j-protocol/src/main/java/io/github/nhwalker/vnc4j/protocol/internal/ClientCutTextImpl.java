package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.ClientCutText;
import java.util.Arrays;

public final class ClientCutTextImpl implements ClientCutText {
    private final byte[] text;

    public ClientCutTextImpl(byte[] text) {
        this.text = text;
    }

    @Override
    public byte[] text() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientCutText other)) return false;
        return Arrays.equals(text, other.text());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(text);
    }

    @Override
    public String toString() {
        return "ClientCutText[text=" + Arrays.toString(text) + "]";
    }

    public static final class BuilderImpl implements ClientCutText.Builder {
        private byte[] text;

        @Override
        public Builder text(byte[] text) {
            this.text = text;
            return this;
        }

        @Override
        public ClientCutText build() {
            return new ClientCutTextImpl(text);
        }
    }
}
