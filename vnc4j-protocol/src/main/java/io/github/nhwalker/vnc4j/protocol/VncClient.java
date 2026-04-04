package io.github.nhwalker.vnc4j.protocol;

/**
 * Client-side application interface for a single VNC server connection.
 *
 * <p>A new instance is created by {@link VncClientFactory} for each outbound
 * connection. The {@link VncSocketClient} drives the RFB protocol handshake
 * and message loop, calling the methods below at the appropriate times. The
 * supplied {@link VncSocketClientHandle} (passed to the factory) is how the
 * implementation sends client-to-server messages.
 *
 * <p>Only security type 1 (None) is supported. Only the required parts of the
 * RFB protocol are dispatched. Extension message types (GII, QEMU, XVP, etc.)
 * are not dispatched.
 */
public interface VncClient {

    /**
     * Called before the handshake to obtain the {@link ClientInit} message that
     * will be sent to the server. The default implementation requests a shared
     * session ({@code shared=true}).
     *
     * @return the client initialisation message to send
     */
    default ClientInit clientInit() {
        return ClientInit.newBuilder().shared(true).build();
    }

    /**
     * Called at the end of the RFB handshake, after {@link ServerInit} is
     * received. Implementations typically store the framebuffer dimensions,
     * pixel format, and desktop name here, then issue an initial
     * {@link FramebufferUpdateRequest} via the {@link VncSocketClientHandle}.
     *
     * @param serverInit the server's initialisation message
     */
    void onServerInit(ServerInit serverInit);

    /**
     * Called when the server sends a framebuffer update. This is the primary
     * display-refresh callback.
     *
     * @param msg the framebuffer update
     */
    void onFramebufferUpdate(FramebufferUpdate msg);

    /**
     * Called when the server sends a colour-map update.
     * Default implementation does nothing.
     *
     * @param msg the colour-map update
     */
    default void onSetColourMapEntries(SetColourMapEntries msg) {}

    /**
     * Called when the server sends a bell notification.
     * Default implementation does nothing.
     *
     * @param msg the bell message
     */
    default void onBell(Bell msg) {}

    /**
     * Called when the server sends clipboard text.
     * Default implementation does nothing.
     *
     * @param msg the clipboard message
     */
    default void onServerCutText(ServerCutText msg) {}

    /**
     * Called when the connection is closed, whether normally or due to an
     * error. Default implementation does nothing.
     */
    default void onClose() {}
}
