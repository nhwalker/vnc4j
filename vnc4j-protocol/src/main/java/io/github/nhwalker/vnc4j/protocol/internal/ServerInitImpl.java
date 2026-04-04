package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.ServerInit;

public record ServerInitImpl(
        int framebufferWidth,
        int framebufferHeight,
        PixelFormat pixelFormat,
        String name
) implements ServerInit {

    public static final class BuilderImpl implements ServerInit.Builder {
        private int framebufferWidth;
        private int framebufferHeight;
        private PixelFormat pixelFormat;
        private String name;

        @Override public Builder framebufferWidth(int v) { this.framebufferWidth = v; return this; }
        @Override public Builder framebufferHeight(int v) { this.framebufferHeight = v; return this; }
        @Override public Builder pixelFormat(PixelFormat v) { this.pixelFormat = v; return this; }
        @Override public Builder name(String v) { this.name = v; return this; }

        @Override
        public ServerInit build() {
            return new ServerInitImpl(framebufferWidth, framebufferHeight, pixelFormat, name);
        }
    }
}
