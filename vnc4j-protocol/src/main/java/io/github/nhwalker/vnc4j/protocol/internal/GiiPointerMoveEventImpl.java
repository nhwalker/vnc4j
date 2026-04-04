package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiPointerMoveEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public record GiiPointerMoveEventImpl(
        int eventType,
        long deviceOrigin,
        int x,
        int y,
        int z,
        int wheel
) implements GiiPointerMoveEvent {

    @Override
    public void write(OutputStream out, boolean bigEndian) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(24); // event-size
        dos.writeByte(eventType);
        GiiIo.writeEU16(dos, bigEndian, 0); // padding
        GiiIo.writeEU32(dos, bigEndian, deviceOrigin);
        GiiIo.writeES32(dos, bigEndian, x);
        GiiIo.writeES32(dos, bigEndian, y);
        GiiIo.writeES32(dos, bigEndian, z);
        GiiIo.writeES32(dos, bigEndian, wheel);
    }

    public static GiiPointerMoveEvent read(InputStream in, boolean bigEndian) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        GiiIo.readEU16(dis, bigEndian); // padding
        long deviceOrigin = GiiIo.readEU32(dis, bigEndian);
        int x = GiiIo.readES32(dis, bigEndian);
        int y = GiiIo.readES32(dis, bigEndian);
        int z = GiiIo.readES32(dis, bigEndian);
        int wheel = GiiIo.readES32(dis, bigEndian);
        return new GiiPointerMoveEventImpl(8, deviceOrigin, x, y, z, wheel);
    }

    static GiiPointerMoveEvent readWithType(InputStream in, boolean bigEndian, int eventType) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        GiiIo.readEU16(dis, bigEndian); // padding
        long deviceOrigin = GiiIo.readEU32(dis, bigEndian);
        int x = GiiIo.readES32(dis, bigEndian);
        int y = GiiIo.readES32(dis, bigEndian);
        int z = GiiIo.readES32(dis, bigEndian);
        int wheel = GiiIo.readES32(dis, bigEndian);
        return new GiiPointerMoveEventImpl(eventType, deviceOrigin, x, y, z, wheel);
    }

    public static final class BuilderImpl implements GiiPointerMoveEvent.Builder {
        private int eventType;
        private long deviceOrigin;
        private int x;
        private int y;
        private int z;
        private int wheel;

        @Override public Builder eventType(int v) { this.eventType = v; return this; }
        @Override public Builder deviceOrigin(long v) { this.deviceOrigin = v; return this; }
        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder z(int v) { this.z = v; return this; }
        @Override public Builder wheel(int v) { this.wheel = v; return this; }

        @Override
        public GiiPointerMoveEvent build() {
            return new GiiPointerMoveEventImpl(eventType, deviceOrigin, x, y, z, wheel);
        }
    }
}
