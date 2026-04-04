package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.ClientCutText;
import io.github.nhwalker.vnc4j.protocol.ClientMessage;
import io.github.nhwalker.vnc4j.protocol.FramebufferUpdateRequest;
import io.github.nhwalker.vnc4j.protocol.KeyEvent;
import io.github.nhwalker.vnc4j.protocol.PointerEvent;
import io.github.nhwalker.vnc4j.protocol.SetEncodings;
import io.github.nhwalker.vnc4j.protocol.SetPixelFormat;

import java.io.IOException;
import java.io.InputStream;

/**
 * Reads the message-type byte from the stream and dispatches to the
 * appropriate {@code read(InputStream)} method for each required client
 * message type.
 */
public final class ClientMessageDispatch {
    private ClientMessageDispatch() {}

    public static ClientMessage read(InputStream in) throws IOException {
        int type = in.read();
        if (type == -1) {
            throw new java.io.EOFException("Connection closed while reading client message type");
        }
        return switch (type) {
            case 0 -> SetPixelFormat.read(in);
            case 2 -> SetEncodings.read(in);
            case 3 -> FramebufferUpdateRequest.read(in);
            case 4 -> KeyEvent.read(in);
            case 5 -> PointerEvent.read(in);
            case 6 -> ClientCutText.read(in);
            default -> throw new UnsupportedOperationException(
                    "Unsupported client message type: " + type);
        };
    }
}
