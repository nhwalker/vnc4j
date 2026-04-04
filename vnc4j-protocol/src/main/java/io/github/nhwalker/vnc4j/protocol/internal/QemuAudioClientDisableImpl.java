package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.QemuAudioClientDisable;

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

    public static final class BuilderImpl implements QemuAudioClientDisable.Builder {
        @Override
        public QemuAudioClientDisable build() {
            return INSTANCE;
        }
    }
}
