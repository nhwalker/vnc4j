package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.SetPixelFormat;

public record SetPixelFormatImpl(PixelFormat pixelFormat) implements SetPixelFormat {

    public static final class BuilderImpl implements SetPixelFormat.Builder {
        private PixelFormat pixelFormat;

        @Override
        public Builder pixelFormat(PixelFormat v) {
            this.pixelFormat = v;
            return this;
        }

        @Override
        public SetPixelFormat build() {
            return new SetPixelFormatImpl(pixelFormat);
        }
    }
}
