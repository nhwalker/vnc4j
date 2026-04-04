package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.QemuAudioClientSetFormat;

public record QemuAudioClientSetFormatImpl(int sampleFormat, int nchannels, long frequency) implements QemuAudioClientSetFormat {

    public static final class BuilderImpl implements QemuAudioClientSetFormat.Builder {
        private int sampleFormat;
        private int nchannels;
        private long frequency;

        @Override public Builder sampleFormat(int v) { this.sampleFormat = v; return this; }
        @Override public Builder nchannels(int v) { this.nchannels = v; return this; }
        @Override public Builder frequency(long v) { this.frequency = v; return this; }

        @Override
        public QemuAudioClientSetFormat build() {
            return new QemuAudioClientSetFormatImpl(sampleFormat, nchannels, frequency);
        }
    }
}
