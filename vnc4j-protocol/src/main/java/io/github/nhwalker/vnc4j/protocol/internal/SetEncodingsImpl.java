package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.messages.SetEncodings;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public record SetEncodingsImpl(List<Integer> encodings) implements SetEncodings {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        List<Integer> encs = encodings != null ? encodings : List.of();
        dos.writeByte(2); // message-type
        dos.writeByte(0); // padding
        dos.writeShort(encs.size());
        for (int enc : encs) {
            dos.writeInt(enc);
        }
    }

    public static SetEncodings read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        dis.readUnsignedByte(); // padding
        int count = dis.readUnsignedShort();
        List<Integer> encs = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            encs.add(dis.readInt());
        }
        return new SetEncodingsImpl(encs);
    }

    public static final class BuilderImpl implements SetEncodings.Builder {
        private List<Integer> encodings;

        @Override
        public Builder encodings(List<Integer> v) {
            this.encodings = v;
            return this;
        }

        @Override
        public SetEncodings build() {
            return new SetEncodingsImpl(encodings);
        }
    }
}
