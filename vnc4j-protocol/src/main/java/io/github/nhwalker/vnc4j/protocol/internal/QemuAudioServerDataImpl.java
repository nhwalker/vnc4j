package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.QemuAudioServerData;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public final class QemuAudioServerDataImpl implements QemuAudioServerData {
    private final byte[] data;

    public QemuAudioServerDataImpl(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] data() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QemuAudioServerData other)) return false;
        return Arrays.equals(data, other.data());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public String toString() {
        return "QemuAudioServerData[data=" + Arrays.toString(data) + "]";
    }

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] d = data != null ? data : new byte[0];
        dos.writeByte(255); // message-type
        dos.writeByte(1);   // sub-type
        dos.writeShort(2);  // operation=2
        dos.writeInt(d.length);
        dos.write(d);
    }

    public static QemuAudioServerData read(InputStream in) throws IOException {
        // sub-type and operation already consumed; read data
        DataInputStream dis = new DataInputStream(in);
        int len = dis.readInt();
        byte[] d = new byte[len];
        dis.readFully(d);
        return new QemuAudioServerDataImpl(d);
    }

    public static final class BuilderImpl implements QemuAudioServerData.Builder {
        private byte[] data;

        @Override
        public Builder data(byte[] data) {
            this.data = data;
            return this;
        }

        @Override
        public QemuAudioServerData build() {
            return new QemuAudioServerDataImpl(data);
        }
    }
}
