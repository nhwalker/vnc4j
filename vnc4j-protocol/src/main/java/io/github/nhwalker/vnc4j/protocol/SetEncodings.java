package io.github.nhwalker.vnc4j.protocol;

import java.util.List;

/** Client message specifying the list of supported encoding types in preferred order. */
public non-sealed interface SetEncodings extends ClientMessage {
    List<Integer> encodings();

    interface Builder {
        Builder encodings(List<Integer> encodings);

        SetEncodings build();

        default Builder from(SetEncodings msg) {
            return encodings(msg.encodings());
        }
    }
}
