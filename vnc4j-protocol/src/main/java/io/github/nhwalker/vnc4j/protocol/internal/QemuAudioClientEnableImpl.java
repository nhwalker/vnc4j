package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.QemuAudioClientEnable;

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

    public static final class BuilderImpl implements QemuAudioClientEnable.Builder {
        @Override
        public QemuAudioClientEnable build() {
            return INSTANCE;
        }
    }
}
