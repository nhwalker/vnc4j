package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.SetEncodings;
import java.util.List;

public record SetEncodingsImpl(List<Integer> encodings) implements SetEncodings {

    public static final class BuilderImpl implements SetEncodings.Builder {
        private List<Integer> encodings;

        @Override
        public Builder encodings(List<Integer> v) {
            this.encodings = v;
            return this;
        }

        @Override
        public SetEncodings build() {
            return new SetEncodingsImpl(encodings);
        }
    }
}
