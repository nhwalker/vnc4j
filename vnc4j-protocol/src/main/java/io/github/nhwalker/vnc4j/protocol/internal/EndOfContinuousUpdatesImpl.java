package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.EndOfContinuousUpdates;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class EndOfContinuousUpdatesImpl implements EndOfContinuousUpdates {

    public static final EndOfContinuousUpdatesImpl INSTANCE = new EndOfContinuousUpdatesImpl();

    private EndOfContinuousUpdatesImpl() {}

    @Override
    public boolean equals(Object o) {
        return o instanceof EndOfContinuousUpdates;
    }

    @Override
    public int hashCode() {
        return EndOfContinuousUpdates.class.hashCode();
    }

    @Override
    public String toString() {
        return "EndOfContinuousUpdates[]";
    }

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(150); // message-type
    }

    public static EndOfContinuousUpdates read(InputStream in) throws IOException {
        // Nothing to read after the type byte
        return INSTANCE;
    }

    public static final class BuilderImpl implements EndOfContinuousUpdates.Builder {
        @Override
        public EndOfContinuousUpdates build() {
            return INSTANCE;
        }
    }
}
