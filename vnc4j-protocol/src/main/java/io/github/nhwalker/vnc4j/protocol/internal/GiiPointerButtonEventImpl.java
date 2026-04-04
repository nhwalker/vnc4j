package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiPointerButtonEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public record GiiPointerButtonEventImpl(int eventType, long deviceOrigin, long buttonNumber) implements GiiPointerButtonEvent {

    @Override
    public void write(OutputStream out, boolean bigEndian) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(12); // event-size
        dos.writeByte(eventType);
        GiiIo.writeEU16(dos, bigEndian, 0); // padding
        GiiIo.writeEU32(dos, bigEndian, deviceOrigin);
        GiiIo.writeEU32(dos, bigEndian, buttonNumber);
    }

    public static GiiPointerButtonEvent read(InputStream in, boolean bigEndian) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        GiiIo.readEU16(dis, bigEndian); // padding
        long deviceOrigin = GiiIo.readEU32(dis, bigEndian);
        long buttonNumber = GiiIo.readEU32(dis, bigEndian);
        return new GiiPointerButtonEventImpl(10, deviceOrigin, buttonNumber);
    }

    static GiiPointerButtonEvent readWithType(InputStream in, boolean bigEndian, int eventType) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        GiiIo.readEU16(dis, bigEndian); // padding
        long deviceOrigin = GiiIo.readEU32(dis, bigEndian);
        long buttonNumber = GiiIo.readEU32(dis, bigEndian);
        return new GiiPointerButtonEventImpl(eventType, deviceOrigin, buttonNumber);
    }

    public static final class BuilderImpl implements GiiPointerButtonEvent.Builder {
        private int eventType;
        private long deviceOrigin;
        private long buttonNumber;

        @Override public Builder eventType(int v) { this.eventType = v; return this; }
        @Override public Builder deviceOrigin(long v) { this.deviceOrigin = v; return this; }
        @Override public Builder buttonNumber(long v) { this.buttonNumber = v; return this; }

        @Override
        public GiiPointerButtonEvent build() {
            return new GiiPointerButtonEventImpl(eventType, deviceOrigin, buttonNumber);
        }
    }
}
