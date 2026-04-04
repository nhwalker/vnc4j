package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.QemuAudioServerEnd;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class QemuAudioServerEndImpl implements QemuAudioServerEnd {

    public static final QemuAudioServerEndImpl INSTANCE = new QemuAudioServerEndImpl();

    private QemuAudioServerEndImpl() {}

    @Override
    public boolean equals(Object o) {
        return o instanceof QemuAudioServerEnd;
    }

    @Override
    public int hashCode() {
        return QemuAudioServerEnd.class.hashCode();
    }

    @Override
    public String toString() {
        return "QemuAudioServerEnd[]";
    }

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(255); // message-type
        dos.writeByte(1);   // sub-type
        dos.writeShort(0);  // operation=0
    }

    public static QemuAudioServerEnd read(InputStream in) throws IOException {
        // sub-type already consumed; nothing more to read
        return INSTANCE;
    }

    public static final class BuilderImpl implements QemuAudioServerEnd.Builder {
        @Override
        public QemuAudioServerEnd build() {
            return INSTANCE;
        }
    }
}
