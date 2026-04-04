package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.QemuAudioClientDisable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class QemuAudioClientDisableImpl implements QemuAudioClientDisable {

    public static final QemuAudioClientDisableImpl INSTANCE = new QemuAudioClientDisableImpl();

    private QemuAudioClientDisableImpl() {}

    @Override
    public boolean equals(Object o) {
        return o instanceof QemuAudioClientDisable;
    }

    @Override
    public int hashCode() {
        return QemuAudioClientDisable.class.hashCode();
    }

    @Override
    public String toString() {
        return "QemuAudioClientDisable[]";
    }

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(255); // message-type
        dos.writeByte(1);   // sub-type
        dos.writeShort(1);  // operation=1
    }

    public static QemuAudioClientDisable read(InputStream in) throws IOException {
        // sub-type already consumed; nothing more to read
        return INSTANCE;
    }

    public static final class BuilderImpl implements QemuAudioClientDisable.Builder {
        @Override
        public QemuAudioClientDisable build() {
            return INSTANCE;
        }
    }
}
