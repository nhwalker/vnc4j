package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.QemuAudioServerEnd;

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

    public static final class BuilderImpl implements QemuAudioServerEnd.Builder {
        @Override
        public QemuAudioServerEnd build() {
            return INSTANCE;
        }
    }
}
