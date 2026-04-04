package io.github.nhwalker.vnc4j.protocol;

/** Describes a single analog valuator (axis) on a GII input device. */
public interface GiiValuator {
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
}
