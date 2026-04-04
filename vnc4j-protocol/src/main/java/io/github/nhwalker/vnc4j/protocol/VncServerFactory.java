package io.github.nhwalker.vnc4j.protocol;

/**
 * Factory that creates a {@link VncServer} for each new client connection.
 *
 * <p>The factory receives a {@link VncSocketServerHandle} that the produced
 * {@code VncServer} can use to send server-to-client messages at any time.
 */
@FunctionalInterface
public interface VncServerFactory {
    /**
     * Creates a new {@link VncServer} for an incoming connection.
     *
     * @param handle the handle through which the server can send messages to
     *               the newly connected client
     * @return a fresh {@link VncServer} instance dedicated to this connection
     */
    VncServer create(VncSocketServerHandle handle);
}
