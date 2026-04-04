package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.GiiValuatorImpl;

/** Describes a single analog valuator (axis) on a GII input device. */
public interface GiiValuator {
    static Builder newBuilder() {
        return new GiiValuatorImpl.BuilderImpl();
    }

    long index();
    String longName();
    String shortName();
    int rangeMin();
    int rangeCenter();
    int rangeMax();
    long siUnit();
    int siAdd();
    int siMul();
    int siDiv();
    int siShift();

    interface Builder {
        Builder index(long index);
        Builder longName(String longName);
        Builder shortName(String shortName);
        Builder rangeMin(int rangeMin);
        Builder rangeCenter(int rangeCenter);
        Builder rangeMax(int rangeMax);
        Builder siUnit(long siUnit);
        Builder siAdd(int siAdd);
        Builder siMul(int siMul);
        Builder siDiv(int siDiv);
        Builder siShift(int siShift);

        GiiValuator build();

        default Builder from(GiiValuator msg) {
            return index(msg.index()).longName(msg.longName()).shortName(msg.shortName())
                    .rangeMin(msg.rangeMin()).rangeCenter(msg.rangeCenter()).rangeMax(msg.rangeMax())
                    .siUnit(msg.siUnit()).siAdd(msg.siAdd()).siMul(msg.siMul()).siDiv(msg.siDiv())
                    .siShift(msg.siShift());
        }
    }
}
