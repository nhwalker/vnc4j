package io.github.nhwalker.vnc4j.protocol.messages;

import io.github.nhwalker.vnc4j.protocol.internal.SecurityTypesImpl;

import java.util.List;

/** Server-to-client list of supported security type codes. */
public non-sealed interface SecurityTypes extends RfbMessage {
    static Builder newBuilder() {
        return new SecurityTypesImpl.BuilderImpl();
    }

    List<Integer> securityTypes();

    interface Builder {
        Builder securityTypes(List<Integer> securityTypes);

        SecurityTypes build();

        default Builder from(SecurityTypes msg) {
            return securityTypes(msg.securityTypes());
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static SecurityTypes read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.SecurityTypesImpl.read(in);
    }
}
