package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.messages.SecurityTypes;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public record SecurityTypesImpl(List<Integer> securityTypes) implements SecurityTypes {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        List<Integer> types = securityTypes != null ? securityTypes : List.of();
        dos.writeByte(types.size());
        for (int t : types) {
            dos.writeByte(t);
        }
    }

    public static SecurityTypes read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        int count = dis.readUnsignedByte();
        if (count == 0) {
            // Read and discard failure reason
            int reasonLen = dis.readInt();
            dis.skipBytes(reasonLen);
            return new SecurityTypesImpl(List.of());
        }
        List<Integer> types = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            types.add(dis.readUnsignedByte());
        }
        return new SecurityTypesImpl(types);
    }

    public static final class BuilderImpl implements SecurityTypes.Builder {
        private List<Integer> securityTypes;

        @Override
        public Builder securityTypes(List<Integer> v) {
            this.securityTypes = v;
            return this;
        }

        @Override
        public SecurityTypes build() {
            return new SecurityTypesImpl(securityTypes);
        }
    }
}
