package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.SecurityTypes;
import java.util.List;

public record SecurityTypesImpl(List<Integer> securityTypes) implements SecurityTypes {

    public static final class BuilderImpl implements SecurityTypes.Builder {
        private List<Integer> securityTypes;

        @Override
        public Builder securityTypes(List<Integer> v) {
            this.securityTypes = v;
            return this;
        }

        @Override
        public SecurityTypes build() {
            return new SecurityTypesImpl(securityTypes);
        }
    }
}
