package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.messages.ColourMapEntry;
import io.github.nhwalker.vnc4j.protocol.messages.SetColourMapEntries;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public record SetColourMapEntriesImpl(int firstColour, List<ColourMapEntry> colours) implements SetColourMapEntries {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        List<ColourMapEntry> cols = colours != null ? colours : List.of();
        dos.writeByte(1); // message-type
        dos.writeByte(0); // padding
        dos.writeShort(firstColour);
        dos.writeShort(cols.size());
        for (ColourMapEntry e : cols) {
            e.write(out);
        }
    }

    public static SetColourMapEntries read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        dis.readUnsignedByte(); // padding
        int firstColour = dis.readUnsignedShort();
        int count = dis.readUnsignedShort();
        List<ColourMapEntry> colours = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            colours.add(ColourMapEntry.read(in));
        }
        return new SetColourMapEntriesImpl(firstColour, colours);
    }

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
