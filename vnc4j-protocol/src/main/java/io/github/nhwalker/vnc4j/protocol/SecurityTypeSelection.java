package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.SecurityTypeSelectionImpl;

/** Client-to-server selection of a single security type to use. */
public non-sealed interface SecurityTypeSelection extends RfbMessage {
    static Builder newBuilder() {
        return new SecurityTypeSelectionImpl.BuilderImpl();
    }

    int securityType();

    interface Builder {
        Builder securityType(int securityType);

        SecurityTypeSelection build();

        default Builder from(SecurityTypeSelection msg) {
            return securityType(msg.securityType());
        }
    }
}
