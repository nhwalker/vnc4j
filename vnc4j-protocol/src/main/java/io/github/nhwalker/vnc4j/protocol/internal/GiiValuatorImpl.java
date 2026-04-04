package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiValuator;

public record GiiValuatorImpl(
        long index,
        String longName,
        String shortName,
        int rangeMin,
        int rangeCenter,
        int rangeMax,
        long siUnit,
        int siAdd,
        int siMul,
        int siDiv,
        int siShift
) implements GiiValuator {

    public static final class BuilderImpl implements GiiValuator.Builder {
        private long index;
        private String longName;
        private String shortName;
        private int rangeMin;
        private int rangeCenter;
        private int rangeMax;
        private long siUnit;
        private int siAdd;
        private int siMul;
        private int siDiv;
        private int siShift;

        @Override public Builder index(long v) { this.index = v; return this; }
        @Override public Builder longName(String v) { this.longName = v; return this; }
        @Override public Builder shortName(String v) { this.shortName = v; return this; }
        @Override public Builder rangeMin(int v) { this.rangeMin = v; return this; }
        @Override public Builder rangeCenter(int v) { this.rangeCenter = v; return this; }
        @Override public Builder rangeMax(int v) { this.rangeMax = v; return this; }
        @Override public Builder siUnit(long v) { this.siUnit = v; return this; }
        @Override public Builder siAdd(int v) { this.siAdd = v; return this; }
        @Override public Builder siMul(int v) { this.siMul = v; return this; }
        @Override public Builder siDiv(int v) { this.siDiv = v; return this; }
        @Override public Builder siShift(int v) { this.siShift = v; return this; }

        @Override
        public GiiValuator build() {
            return new GiiValuatorImpl(index, longName, shortName, rangeMin, rangeCenter,
                    rangeMax, siUnit, siAdd, siMul, siDiv, siShift);
        }
    }
}
