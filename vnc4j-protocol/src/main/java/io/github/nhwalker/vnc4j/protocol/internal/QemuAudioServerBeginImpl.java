package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.QemuAudioServerBegin;

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

    public static final class BuilderImpl implements QemuAudioServerBegin.Builder {
        @Override
        public QemuAudioServerBegin build() {
            return INSTANCE;
        }
    }
}
