package io.github.nhwalker.vnc4j.protocol;

import java.util.List;

/** Server message setting one or more colour map (palette) entries starting at a given index. */
public non-sealed interface SetColourMapEntries extends ServerMessage {
    int firstColour();
    List<ColourMapEntry> colours();
}
