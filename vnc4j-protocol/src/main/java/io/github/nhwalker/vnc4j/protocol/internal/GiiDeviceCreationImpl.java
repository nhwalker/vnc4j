package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiDeviceCreation;
import io.github.nhwalker.vnc4j.protocol.GiiValuator;
import java.util.List;

public record GiiDeviceCreationImpl(
        boolean bigEndian,
        String deviceName,
        long vendorId,
        long productId,
        long canGenerate,
        long numRegisters,
        long numButtons,
        List<GiiValuator> valuators
) implements GiiDeviceCreation {

    public static final class BuilderImpl implements GiiDeviceCreation.Builder {
        private boolean bigEndian;
        private String deviceName;
        private long vendorId;
        private long productId;
        private long canGenerate;
        private long numRegisters;
        private long numButtons;
        private List<GiiValuator> valuators;

        @Override public Builder bigEndian(boolean v) { this.bigEndian = v; return this; }
        @Override public Builder deviceName(String v) { this.deviceName = v; return this; }
        @Override public Builder vendorId(long v) { this.vendorId = v; return this; }
        @Override public Builder productId(long v) { this.productId = v; return this; }
        @Override public Builder canGenerate(long v) { this.canGenerate = v; return this; }
        @Override public Builder numRegisters(long v) { this.numRegisters = v; return this; }
        @Override public Builder numButtons(long v) { this.numButtons = v; return this; }
        @Override public Builder valuators(List<GiiValuator> v) { this.valuators = v; return this; }

        @Override
        public GiiDeviceCreation build() {
            return new GiiDeviceCreationImpl(bigEndian, deviceName, vendorId, productId,
                    canGenerate, numRegisters, numButtons, valuators);
        }
    }
}
