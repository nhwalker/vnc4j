package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.messages.ColourMapEntry;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public record ColourMapEntryImpl(int red, int green, int blue) implements ColourMapEntry {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeShort(red);
        dos.writeShort(green);
        dos.writeShort(blue);
    }

    public static ColourMapEntry read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        int red = dis.readUnsignedShort();
        int green = dis.readUnsignedShort();
        int blue = dis.readUnsignedShort();
        return new ColourMapEntryImpl(red, green, blue);
    }

    public static final class BuilderImpl implements ColourMapEntry.Builder {
        private int red;
        private int green;
        private int blue;

        @Override public Builder red(int v) { this.red = v; return this; }
        @Override public Builder green(int v) { this.green = v; return this; }
        @Override public Builder blue(int v) { this.blue = v; return this; }

        @Override
        public ColourMapEntry build() {
            return new ColourMapEntryImpl(red, green, blue);
        }
    }
}
