package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.QemuAudioServerData;
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
