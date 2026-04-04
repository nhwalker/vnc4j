package io.github.nhwalker.vnc4j.protocol;

import java.util.List;

/** Server-to-client list of supported security type codes. */
public non-sealed interface SecurityTypes extends RfbMessage {
    List<Integer> securityTypes();
}
