package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.FramebufferUpdateRequest;

public record FramebufferUpdateRequestImpl(
        boolean incremental,
        int x,
        int y,
        int width,
        int height
) implements FramebufferUpdateRequest {

    public static final class BuilderImpl implements FramebufferUpdateRequest.Builder {
        private boolean incremental;
        private int x;
        private int y;
        private int width;
        private int height;

        @Override public Builder incremental(boolean v) { this.incremental = v; return this; }
        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }

        @Override
        public FramebufferUpdateRequest build() {
            return new FramebufferUpdateRequestImpl(incremental, x, y, width, height);
        }
    }
}
