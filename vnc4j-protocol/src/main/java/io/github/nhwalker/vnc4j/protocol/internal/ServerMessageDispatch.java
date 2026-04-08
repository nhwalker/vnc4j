package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.messages.Bell;
import io.github.nhwalker.vnc4j.protocol.messages.FramebufferUpdate;
import io.github.nhwalker.vnc4j.protocol.messages.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.messages.ServerCutText;
import io.github.nhwalker.vnc4j.protocol.messages.ServerMessage;
import io.github.nhwalker.vnc4j.protocol.messages.SetColourMapEntries;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads the message-type byte from the stream and dispatches to the
 * appropriate {@code read} method for each required server message type.
 *
 * <p>Symmetric to {@link ClientMessageDispatch} on the server side. The
 * {@code pixelFormat} parameter is forwarded to {@link FramebufferUpdate#read}
 * so encoding-specific parsers know the bytes-per-pixel and colour depth.
 */
public final class ServerMessageDispatch {
    private ServerMessageDispatch() {}

    public static ServerMessage read(InputStream in, PixelFormat pixelFormat)
            throws IOException {
        int type = in.read();
        if (type == -1) {
            throw new EOFException("Connection closed while reading server message type");
        }
        return switch (type) {
            case 0 -> FramebufferUpdate.read(in, pixelFormat);
            case 1 -> SetColourMapEntries.read(in);
            case 2 -> Bell.read(in);
            case 3 -> ServerCutText.read(in);
            default -> throw new UnsupportedOperationException(
                    "Unsupported server message type: " + type);
        };
    }
}
