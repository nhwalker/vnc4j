package io.github.nhwalker.vnc4j.protocol;

/** Client initialisation message indicating whether a shared session is requested. */
public non-sealed interface ClientInit extends RfbMessage {
    boolean shared();
}
