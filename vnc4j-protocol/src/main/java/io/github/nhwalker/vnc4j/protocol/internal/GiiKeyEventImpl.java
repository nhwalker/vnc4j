package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiKeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public record GiiKeyEventImpl(
        int eventType,
        long deviceOrigin,
        long modifiers,
        long symbol,
        long label,
        long button
) implements GiiKeyEvent {

    @Override
    public void write(OutputStream out, boolean bigEndian) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(24); // event-size
        dos.writeByte(eventType);
        GiiIo.writeEU16(dos, bigEndian, 0); // padding
        GiiIo.writeEU32(dos, bigEndian, deviceOrigin);
        GiiIo.writeEU32(dos, bigEndian, modifiers);
        GiiIo.writeEU32(dos, bigEndian, symbol);
        GiiIo.writeEU32(dos, bigEndian, label);
        GiiIo.writeEU32(dos, bigEndian, button);
    }

    public static GiiKeyEvent read(InputStream in, boolean bigEndian) throws IOException {
        // event-size and event-type already read by dispatcher; read remaining 22 bytes
        DataInputStream dis = new DataInputStream(in);
        GiiIo.readEU16(dis, bigEndian); // padding
        long deviceOrigin = GiiIo.readEU32(dis, bigEndian);
        long modifiers = GiiIo.readEU32(dis, bigEndian);
        long symbol = GiiIo.readEU32(dis, bigEndian);
        long label = GiiIo.readEU32(dis, bigEndian);
        long button = GiiIo.readEU32(dis, bigEndian);
        return new GiiKeyEventImpl(0, deviceOrigin, modifiers, symbol, label, button);
    }

    // Read with eventType already known (called from dispatcher)
    static GiiKeyEvent readWithType(InputStream in, boolean bigEndian, int eventType) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        GiiIo.readEU16(dis, bigEndian); // padding
        long deviceOrigin = GiiIo.readEU32(dis, bigEndian);
        long modifiers = GiiIo.readEU32(dis, bigEndian);
        long symbol = GiiIo.readEU32(dis, bigEndian);
        long label = GiiIo.readEU32(dis, bigEndian);
        long button = GiiIo.readEU32(dis, bigEndian);
        return new GiiKeyEventImpl(eventType, deviceOrigin, modifiers, symbol, label, button);
    }

    public static final class BuilderImpl implements GiiKeyEvent.Builder {
        private int eventType;
        private long deviceOrigin;
        private long modifiers;
        private long symbol;
        private long label;
        private long button;

        @Override public Builder eventType(int v) { this.eventType = v; return this; }
        @Override public Builder deviceOrigin(long v) { this.deviceOrigin = v; return this; }
        @Override public Builder modifiers(long v) { this.modifiers = v; return this; }
        @Override public Builder symbol(long v) { this.symbol = v; return this; }
        @Override public Builder label(long v) { this.label = v; return this; }
        @Override public Builder button(long v) { this.button = v; return this; }

        @Override
        public GiiKeyEvent build() {
            return new GiiKeyEventImpl(eventType, deviceOrigin, modifiers, symbol, label, button);
        }
    }
}
