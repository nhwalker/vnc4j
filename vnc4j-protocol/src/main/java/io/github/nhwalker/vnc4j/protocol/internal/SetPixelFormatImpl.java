package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.messages.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.messages.SetPixelFormat;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public record SetPixelFormatImpl(PixelFormat pixelFormat) implements SetPixelFormat {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(0); // message-type
        dos.writeByte(0); // padding
        dos.writeByte(0);
        dos.writeByte(0);
        pixelFormat.write(out);
    }

    public static SetPixelFormat read(InputStream in) throws IOException {
        // skip 3 padding bytes
        in.read(); in.read(); in.read();
        PixelFormat pf = PixelFormat.read(in);
        return new SetPixelFormatImpl(pf);
    }

    public static final class BuilderImpl implements SetPixelFormat.Builder {
        private PixelFormat pixelFormat;

        @Override
        public Builder pixelFormat(PixelFormat v) {
            this.pixelFormat = v;
            return this;
        }

        @Override
        public SetPixelFormat build() {
            return new SetPixelFormatImpl(pixelFormat);
        }
    }
}
