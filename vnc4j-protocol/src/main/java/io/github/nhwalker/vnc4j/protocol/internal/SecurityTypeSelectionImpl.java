package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.messages.SecurityTypeSelection;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public record SecurityTypeSelectionImpl(int securityType) implements SecurityTypeSelection {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(securityType);
    }

    public static SecurityTypeSelection read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        int type = dis.readUnsignedByte();
        return new SecurityTypeSelectionImpl(type);
    }

    public static final class BuilderImpl implements SecurityTypeSelection.Builder {
        private int securityType;

        @Override
        public Builder securityType(int v) {
            this.securityType = v;
            return this;
        }

        @Override
        public SecurityTypeSelection build() {
            return new SecurityTypeSelectionImpl(securityType);
        }
    }
}
