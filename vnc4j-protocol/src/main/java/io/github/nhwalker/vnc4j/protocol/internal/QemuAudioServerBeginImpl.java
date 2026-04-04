package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.QemuAudioServerBegin;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class QemuAudioServerBeginImpl implements QemuAudioServerBegin {

    public static final QemuAudioServerBeginImpl INSTANCE = new QemuAudioServerBeginImpl();

    private QemuAudioServerBeginImpl() {}

    @Override
    public boolean equals(Object o) {
        return o instanceof QemuAudioServerBegin;
    }

    @Override
    public int hashCode() {
        return QemuAudioServerBegin.class.hashCode();
    }

    @Override
    public String toString() {
        return "QemuAudioServerBegin[]";
    }

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(255); // message-type
        dos.writeByte(1);   // sub-type
        dos.writeShort(1);  // operation=1
    }

    public static QemuAudioServerBegin read(InputStream in) throws IOException {
        // sub-type already consumed; nothing more to read
        return INSTANCE;
    }

    public static final class BuilderImpl implements QemuAudioServerBegin.Builder {
        @Override
        public QemuAudioServerBegin build() {
            return INSTANCE;
        }
    }
}
