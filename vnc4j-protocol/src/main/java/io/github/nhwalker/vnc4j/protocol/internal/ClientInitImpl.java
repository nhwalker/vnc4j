package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.ClientInit;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public record ClientInitImpl(boolean shared) implements ClientInit {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(shared ? 1 : 0);
    }

    public static ClientInit read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        boolean shared = dis.readUnsignedByte() != 0;
        return new ClientInitImpl(shared);
    }

    public static final class BuilderImpl implements ClientInit.Builder {
        private boolean shared;

        @Override
        public Builder shared(boolean v) {
            this.shared = v;
            return this;
        }

        @Override
        public ClientInit build() {
            return new ClientInitImpl(shared);
        }
    }
}
