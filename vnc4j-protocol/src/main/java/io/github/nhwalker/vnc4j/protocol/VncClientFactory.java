package io.github.nhwalker.vnc4j.protocol;

/**
 * Factory that creates a {@link VncClient} for a single outbound VNC connection.
 *
 * <p>The factory receives a {@link VncSocketClientHandle} that the produced
 * {@code VncClient} can use to send client-to-server messages at any time.
 */
@FunctionalInterface
public interface VncClientFactory {
    /**
     * Creates a new {@link VncClient} for an outbound connection.
     *
     * @param handle the handle through which the client can send messages to
     *               the connected server
     * @return a fresh {@link VncClient} instance dedicated to this connection
     */
    VncClient create(VncSocketClientHandle handle);
}
