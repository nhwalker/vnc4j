package io.github.nhwalker.vnc4j.protocol;

/** A single RGB entry in a colour map (palette). */
public interface ColourMapEntry {
    int red();
    int green();
    int blue();

    interface Builder {
        Builder red(int red);
        Builder green(int green);
        Builder blue(int blue);

        ColourMapEntry build();

        default Builder from(ColourMapEntry msg) {
            return red(msg.red()).green(msg.green()).blue(msg.blue());
        }
    }
}
