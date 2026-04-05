package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiEvent;
import io.github.nhwalker.vnc4j.protocol.GiiInjectEvents;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public record GiiInjectEventsImpl(boolean bigEndian, List<GiiEvent> events) implements GiiInjectEvents {

    @Override
    public void write(OutputStream out) throws IOException {
        // First serialize all events to compute total length
        ByteArrayOutputStream evBuf = new ByteArrayOutputStream();
        List<GiiEvent> evs = events != null ? events : List.of();
        for (GiiEvent e : evs) {
            e.write(evBuf, bigEndian);
        }
        byte[] evBytes = evBuf.toByteArray();

        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(253); // message-type
        dos.writeByte(bigEndian ? 0x80 : 0x00); // endian-and-sub-type
        GiiIo.writeEU16(dos, bigEndian, evBytes.length);
        dos.write(evBytes);
    }

    public static GiiInjectEvents read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        int endianAndSubType = dis.readUnsignedByte();
        boolean bigEndian = (endianAndSubType & 0x80) != 0;
        int length = GiiIo.readEU16(dis, bigEndian);
        byte[] evBytes = new byte[length];
        dis.readFully(evBytes);
        ByteArrayInputStream bais = new ByteArrayInputStream(evBytes);
        List<GiiEvent> events = new ArrayList<>();
        while (bais.available() > 0) {
            GiiEvent event = GiiEvent.readEvent(bais, bigEndian);
            if (event != null) {
                events.add(event);
            }
        }
        return new GiiInjectEventsImpl(bigEndian, events);
    }

    public static final class BuilderImpl implements GiiInjectEvents.Builder {
        private boolean bigEndian;
        private List<GiiEvent> events;

        @Override public Builder bigEndian(boolean v) { this.bigEndian = v; return this; }
        @Override public Builder events(List<GiiEvent> v) { this.events = v; return this; }

        @Override
        public GiiInjectEvents build() {
            return new GiiInjectEventsImpl(bigEndian, events);
        }
    }
}
