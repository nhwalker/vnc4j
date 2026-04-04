package io.github.nhwalker.vnc4j.protocol;

/** A single RGB entry in a colour map (palette). */
public interface ColourMapEntry {
    int red();
    int green();
    int blue();
}
