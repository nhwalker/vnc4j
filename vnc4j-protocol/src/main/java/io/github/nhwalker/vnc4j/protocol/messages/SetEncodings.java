package io.github.nhwalker.vnc4j.protocol.messages;

import io.github.nhwalker.vnc4j.protocol.internal.SetEncodingsImpl;

import java.util.List;

/** Client message specifying the list of supported encoding types in preferred order. */
public non-sealed interface SetEncodings extends ClientMessage {
    static Builder newBuilder() {
        return new SetEncodingsImpl.BuilderImpl();
    }

    List<Integer> encodings();

    interface Builder {
        Builder encodings(List<Integer> encodings);

        SetEncodings build();

        default Builder from(SetEncodings msg) {
            return encodings(msg.encodings());
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static SetEncodings read(java.io.InputStream in) throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.SetEncodingsImpl.read(in);
    }
}
