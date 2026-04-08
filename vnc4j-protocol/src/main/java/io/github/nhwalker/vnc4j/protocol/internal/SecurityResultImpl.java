package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.messages.SecurityResult;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public record SecurityResultImpl(int status, String failureReason) implements SecurityResult {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeInt(status);
        if (status != 0 && failureReason != null) {
            byte[] reason = failureReason.getBytes(StandardCharsets.UTF_8);
            dos.writeInt(reason.length);
            dos.write(reason);
        }
    }

    public static SecurityResult read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        int status = dis.readInt();
        String reason = null;
        if (status != 0) {
            int len = dis.readInt();
            byte[] buf = new byte[len];
            dis.readFully(buf);
            reason = new String(buf, StandardCharsets.UTF_8);
        }
        return new SecurityResultImpl(status, reason);
    }

    public static final class BuilderImpl implements SecurityResult.Builder {
        private int status;
        private String failureReason;

        @Override public Builder status(int v) { this.status = v; return this; }
        @Override public Builder failureReason(String v) { this.failureReason = v; return this; }

        @Override
        public SecurityResult build() {
            return new SecurityResultImpl(status, failureReason);
        }
    }
}
