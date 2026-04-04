package io.github.nhwalker.vnc4j.protocol;

import java.io.Closeable;
import java.io.IOException;

/**
 * A handle given to a {@link VncClient} that allows it to send
 * client-to-server messages and close the connection.
 *
 * <p>Implementations are thread-safe: {@code send} may be called from any
 * thread, including threads other than the one running the message loop.
 */
public interface VncSocketClientHandle extends Closeable {

    /**
     * Sends a client-to-server message to the connected server.
     *
     * @param message the message to send
     * @throws IOException if the write fails or the connection is closed
     */
    void send(ClientMessage message) throws IOException;

    /**
     * Closes the connection to the server.
     *
     * @throws IOException if an I/O error occurs while closing
     */
    @Override
    void close() throws IOException;
}
