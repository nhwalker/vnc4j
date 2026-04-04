package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiValuatorEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public record GiiValuatorEventImpl(int eventType, long deviceOrigin, long first, List<Integer> values) implements GiiValuatorEvent {

    @Override
    public void write(OutputStream out, boolean bigEndian) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        List<Integer> vals = values != null ? values : List.of();
        int count = vals.size();
        dos.writeByte(16 + 4 * count); // event-size
        dos.writeByte(eventType);
        GiiIo.writeEU16(dos, bigEndian, 0); // padding
        GiiIo.writeEU32(dos, bigEndian, deviceOrigin);
        GiiIo.writeEU32(dos, bigEndian, first);
        GiiIo.writeEU32(dos, bigEndian, count);
        for (int v : vals) {
            GiiIo.writeES32(dos, bigEndian, v);
        }
    }

    public static GiiValuatorEvent read(InputStream in, boolean bigEndian) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        GiiIo.readEU16(dis, bigEndian); // padding
        long deviceOrigin = GiiIo.readEU32(dis, bigEndian);
        long first = GiiIo.readEU32(dis, bigEndian);
        long count = GiiIo.readEU32(dis, bigEndian);
        List<Integer> values = new ArrayList<>((int) count);
        for (int i = 0; i < count; i++) {
            values.add(GiiIo.readES32(dis, bigEndian));
        }
        return new GiiValuatorEventImpl(12, deviceOrigin, first, values);
    }

    static GiiValuatorEvent readWithType(InputStream in, boolean bigEndian, int eventType) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        GiiIo.readEU16(dis, bigEndian); // padding
        long deviceOrigin = GiiIo.readEU32(dis, bigEndian);
        long first = GiiIo.readEU32(dis, bigEndian);
        long count = GiiIo.readEU32(dis, bigEndian);
        List<Integer> values = new ArrayList<>((int) count);
        for (int i = 0; i < count; i++) {
            values.add(GiiIo.readES32(dis, bigEndian));
        }
        return new GiiValuatorEventImpl(eventType, deviceOrigin, first, values);
    }

    public static final class BuilderImpl implements GiiValuatorEvent.Builder {
        private int eventType;
        private long deviceOrigin;
        private long first;
        private List<Integer> values;

        @Override public Builder eventType(int v) { this.eventType = v; return this; }
        @Override public Builder deviceOrigin(long v) { this.deviceOrigin = v; return this; }
        @Override public Builder first(long v) { this.first = v; return this; }
        @Override public Builder values(List<Integer> v) { this.values = v; return this; }

        @Override
        public GiiValuatorEvent build() {
            return new GiiValuatorEventImpl(eventType, deviceOrigin, first, values);
        }
    }
}
