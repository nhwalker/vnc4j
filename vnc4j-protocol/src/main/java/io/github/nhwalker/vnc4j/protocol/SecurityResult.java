package io.github.nhwalker.vnc4j.protocol;

/** Server-to-client result of the security handshake (0=OK, 1=failed, 2=failed-too-many). */
public non-sealed interface SecurityResult extends RfbMessage {
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
}
