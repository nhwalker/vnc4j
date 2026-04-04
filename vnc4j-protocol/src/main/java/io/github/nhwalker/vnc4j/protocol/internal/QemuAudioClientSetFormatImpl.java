package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.QemuAudioClientSetFormat;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public record QemuAudioClientSetFormatImpl(int sampleFormat, int nchannels, long frequency) implements QemuAudioClientSetFormat {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(255); // message-type
        dos.writeByte(1);   // sub-type
        dos.writeShort(2);  // operation=2
        dos.writeByte(sampleFormat);
        dos.writeByte(nchannels);
        dos.writeInt((int)(frequency & 0xFFFFFFFFL));
    }

    public static QemuAudioClientSetFormat read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        // sub-type already consumed; read operation (already known=2), then fields
        // Actually read() is called after type byte (255), sub-type (1), and operation (2) are consumed
        int sampleFormat = dis.readUnsignedByte();
        int nchannels = dis.readUnsignedByte();
        long frequency = Integer.toUnsignedLong(dis.readInt());
        return new QemuAudioClientSetFormatImpl(sampleFormat, nchannels, frequency);
    }

    public static final class BuilderImpl implements QemuAudioClientSetFormat.Builder {
        private int sampleFormat;
        private int nchannels;
        private long frequency;

        @Override public Builder sampleFormat(int v) { this.sampleFormat = v; return this; }
        @Override public Builder nchannels(int v) { this.nchannels = v; return this; }
        @Override public Builder frequency(long v) { this.frequency = v; return this; }

        @Override
        public QemuAudioClientSetFormat build() {
            return new QemuAudioClientSetFormatImpl(sampleFormat, nchannels, frequency);
        }
    }
}
