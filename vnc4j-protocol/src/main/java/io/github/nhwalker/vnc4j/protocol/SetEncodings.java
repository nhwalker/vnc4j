package io.github.nhwalker.vnc4j.protocol;

import java.util.List;

/** Client message specifying the list of supported encoding types in preferred order. */
public interface SetEncodings extends ClientMessage {
    List<Integer> encodings();
}
