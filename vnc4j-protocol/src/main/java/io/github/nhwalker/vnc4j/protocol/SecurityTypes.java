package io.github.nhwalker.vnc4j.protocol;

import java.util.List;

/** Server-to-client list of supported security type codes. */
public interface SecurityTypes extends RfbMessage {
    List<Integer> securityTypes();
}
