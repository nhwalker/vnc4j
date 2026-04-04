package io.github.nhwalker.vnc4j.protocol;

/** Client-to-server selection of a single security type to use. */
public non-sealed interface SecurityTypeSelection extends RfbMessage {
    int securityType();
}
