package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.messages.ProtocolVersion;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public record ProtocolVersionImpl(int major, int minor) implements ProtocolVersion {

    @Override
    public void write(OutputStream out) throws IOException {
        // "RFB xxx.yyy\n" - 12 ASCII bytes
        String s = String.format("RFB %03d.%03d\n", major, minor);
        out.write(s.getBytes(StandardCharsets.US_ASCII));
    }

    public static ProtocolVersion read(InputStream in) throws IOException {
        byte[] buf = new byte[12];
        new DataInputStream(in).readFully(buf);
        String s = new String(buf, StandardCharsets.US_ASCII);
        // Format: "RFB xxx.yyy\n"
        int maj = Integer.parseInt(s.substring(4, 7));
        int min = Integer.parseInt(s.substring(8, 11));
        return new ProtocolVersionImpl(maj, min);
    }

    public static final class BuilderImpl implements ProtocolVersion.Builder {
        private int major;
        private int minor;

        @Override public Builder major(int v) { this.major = v; return this; }
        @Override public Builder minor(int v) { this.minor = v; return this; }

        @Override
        public ProtocolVersion build() {
            return new ProtocolVersionImpl(major, minor);
        }
    }
}
