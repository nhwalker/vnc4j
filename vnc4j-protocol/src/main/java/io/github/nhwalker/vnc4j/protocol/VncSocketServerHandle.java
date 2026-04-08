package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.messages.*;

import java.io.Closeable;
import java.io.IOException;

/**
 * A handle given to a {@link VncServer} that allows it to send
 * server-to-client messages and close the connection.
 *
 * <p>Implementations are thread-safe: {@code send} may be called from any
 * thread, including threads other than the one running the message loop.
 */
public interface VncSocketServerHandle extends Closeable {

    /**
     * Sends a server-to-client message to the connected client.
     *
     * @param message the message to send
     * @throws IOException if the write fails or the connection is closed
     */
    void send(ServerMessage message) throws IOException;

    /**
     * Closes the connection to the client.
     *
     * @throws IOException if an I/O error occurs while closing
     */
    @Override
    void close() throws IOException;
}
