package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.messages.Bell;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class BellImpl implements Bell {

    public static final BellImpl INSTANCE = new BellImpl();

    private BellImpl() {}

    @Override
    public boolean equals(Object o) {
        return o instanceof Bell;
    }

    @Override
    public int hashCode() {
        return Bell.class.hashCode();
    }

    @Override
    public String toString() {
        return "Bell[]";
    }

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(2); // message-type
    }

    public static Bell read(InputStream in) throws IOException {
        // Nothing to read after the type byte
        return INSTANCE;
    }

    public static final class BuilderImpl implements Bell.Builder {
        @Override
        public Bell build() {
            return INSTANCE;
        }
    }
}
