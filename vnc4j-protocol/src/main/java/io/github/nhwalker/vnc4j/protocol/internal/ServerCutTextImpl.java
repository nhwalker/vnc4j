package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.ServerCutText;
import java.util.Arrays;

public final class ServerCutTextImpl implements ServerCutText {
    private final byte[] text;

    public ServerCutTextImpl(byte[] text) {
        this.text = text;
    }

    @Override
    public byte[] text() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerCutText other)) return false;
        return Arrays.equals(text, other.text());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(text);
    }

    @Override
    public String toString() {
        return "ServerCutText[text=" + Arrays.toString(text) + "]";
    }

    public static final class BuilderImpl implements ServerCutText.Builder {
        private byte[] text;

        @Override
        public Builder text(byte[] text) {
            this.text = text;
            return this;
        }

        @Override
        public ServerCutText build() {
            return new ServerCutTextImpl(text);
        }
    }
}
