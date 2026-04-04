package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.FramebufferUpdate;
import io.github.nhwalker.vnc4j.protocol.RfbRectangle;
import java.util.List;

public record FramebufferUpdateImpl(List<RfbRectangle> rectangles) implements FramebufferUpdate {

    public static final class BuilderImpl implements FramebufferUpdate.Builder {
        private List<RfbRectangle> rectangles;

        @Override
        public Builder rectangles(List<RfbRectangle> v) {
            this.rectangles = v;
            return this;
        }

        @Override
        public FramebufferUpdate build() {
            return new FramebufferUpdateImpl(rectangles);
        }
    }
}
