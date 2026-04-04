package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.GiiDeviceCreationImpl;

import java.util.List;

/** GII client message requesting creation of a virtual input device. */
public non-sealed interface GiiDeviceCreation extends GiiClientMessage {
    static Builder newBuilder() {
        return new GiiDeviceCreationImpl.BuilderImpl();
    }

    boolean bigEndian();
    String deviceName();
    long vendorId();
    long productId();
    long canGenerate();
    long numRegisters();
    long numButtons();
    List<GiiValuator> valuators();

    interface Builder {
        Builder bigEndian(boolean bigEndian);
        Builder deviceName(String deviceName);
        Builder vendorId(long vendorId);
        Builder productId(long productId);
        Builder canGenerate(long canGenerate);
        Builder numRegisters(long numRegisters);
        Builder numButtons(long numButtons);
        Builder valuators(List<GiiValuator> valuators);

        GiiDeviceCreation build();

        default Builder from(GiiDeviceCreation msg) {
            return bigEndian(msg.bigEndian()).deviceName(msg.deviceName()).vendorId(msg.vendorId())
                    .productId(msg.productId()).canGenerate(msg.canGenerate()).numRegisters(msg.numRegisters())
                    .numButtons(msg.numButtons()).valuators(msg.valuators());
        }
    }
}
