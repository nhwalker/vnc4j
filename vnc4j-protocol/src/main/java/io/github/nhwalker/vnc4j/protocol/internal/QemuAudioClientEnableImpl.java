package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.QemuAudioClientEnable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class QemuAudioClientEnableImpl implements QemuAudioClientEnable {

    public static final QemuAudioClientEnableImpl INSTANCE = new QemuAudioClientEnableImpl();

    private QemuAudioClientEnableImpl() {}

    @Override
    public boolean equals(Object o) {
        return o instanceof QemuAudioClientEnable;
    }

    @Override
    public int hashCode() {
        return QemuAudioClientEnable.class.hashCode();
    }

    @Override
    public String toString() {
        return "QemuAudioClientEnable[]";
    }

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(255); // message-type
        dos.writeByte(1);   // sub-type
        dos.writeShort(0);  // operation=0
    }

    public static QemuAudioClientEnable read(InputStream in) throws IOException {
        // sub-type already consumed; nothing more to read
        return INSTANCE;
    }

    public static final class BuilderImpl implements QemuAudioClientEnable.Builder {
        @Override
        public QemuAudioClientEnable build() {
            return INSTANCE;
        }
    }
}
