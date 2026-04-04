package io.github.nhwalker.vnc4j.protocol;

import java.util.List;

/** GII client message requesting creation of a virtual input device. */
public non-sealed interface GiiDeviceCreation extends GiiClientMessage {
    boolean bigEndian();
    String deviceName();
    long vendorId();
    long productId();
    long canGenerate();
    long numRegisters();
    long numButtons();
    List<GiiValuator> valuators();
}
