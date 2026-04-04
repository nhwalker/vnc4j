package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.SecurityResultImpl;

/** Server-to-client result of the security handshake (0=OK, 1=failed, 2=failed-too-many). */
public non-sealed interface SecurityResult extends RfbMessage {
    static Builder newBuilder() {
        return new SecurityResultImpl.BuilderImpl();
    }

    int status();
    String failureReason();

    interface Builder {
        Builder status(int status);
        Builder failureReason(String failureReason);

        SecurityResult build();

        default Builder from(SecurityResult msg) {
            return status(msg.status()).failureReason(msg.failureReason());
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static SecurityResult read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.SecurityResultImpl.read(in);
    }
}
