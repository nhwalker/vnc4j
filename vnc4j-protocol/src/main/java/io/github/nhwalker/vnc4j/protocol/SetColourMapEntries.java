package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.SetColourMapEntriesImpl;

import java.util.List;

/** Server message setting one or more colour map (palette) entries starting at a given index. */
public non-sealed interface SetColourMapEntries extends ServerMessage {
    static Builder newBuilder() {
        return new SetColourMapEntriesImpl.BuilderImpl();
    }

    int firstColour();
    List<ColourMapEntry> colours();

    interface Builder {
        Builder firstColour(int firstColour);
        Builder colours(List<ColourMapEntry> colours);

        SetColourMapEntries build();

        default Builder from(SetColourMapEntries msg) {
            return firstColour(msg.firstColour()).colours(msg.colours());
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static SetColourMapEntries read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.SetColourMapEntriesImpl.read(in);
    }
}
