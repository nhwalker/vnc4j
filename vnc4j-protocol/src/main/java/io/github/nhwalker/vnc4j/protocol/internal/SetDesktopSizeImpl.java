package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.Screen;
import io.github.nhwalker.vnc4j.protocol.SetDesktopSize;
import java.util.List;

public record SetDesktopSizeImpl(int width, int height, List<Screen> screens) implements SetDesktopSize {

    public static final class BuilderImpl implements SetDesktopSize.Builder {
        private int width;
        private int height;
        private List<Screen> screens;

        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }
        @Override public Builder screens(List<Screen> v) { this.screens = v; return this; }

        @Override
        public SetDesktopSize build() {
            return new SetDesktopSizeImpl(width, height, screens);
        }
    }
}
