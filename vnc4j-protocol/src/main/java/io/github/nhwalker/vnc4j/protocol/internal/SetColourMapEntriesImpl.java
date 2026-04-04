package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.ColourMapEntry;
import io.github.nhwalker.vnc4j.protocol.SetColourMapEntries;
import java.util.List;

public record SetColourMapEntriesImpl(int firstColour, List<ColourMapEntry> colours) implements SetColourMapEntries {

    public static final class BuilderImpl implements SetColourMapEntries.Builder {
        private int firstColour;
        private List<ColourMapEntry> colours;

        @Override public Builder firstColour(int v) { this.firstColour = v; return this; }
        @Override public Builder colours(List<ColourMapEntry> v) { this.colours = v; return this; }

        @Override
        public SetColourMapEntries build() {
            return new SetColourMapEntriesImpl(firstColour, colours);
        }
    }
}
