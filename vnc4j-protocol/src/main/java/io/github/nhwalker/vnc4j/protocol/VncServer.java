package io.github.nhwalker.vnc4j.protocol;

/**
 * Server-side application interface for a single VNC client connection.
 *
 * <p>A new instance is created by {@link VncServerFactory} for each
 * accepted connection. The {@link VncSocketServer} drives the RFB protocol
 * handshake and message loop, calling the methods below at the appropriate
 * times. The supplied {@link VncSocketServerHandle} (passed to the factory)
 * is how the implementation sends server-to-client messages back.
 *
 * <p>Only the required parts of the RFB protocol are covered here.
 * Extension message types (GII, QEMU, XVP, etc.) are not dispatched.
 */
public interface VncServer {

    /**
     * Called at the end of the RFB handshake, after {@link ClientInit} is
     * received. The returned {@link ServerInit} is sent to the client to
     * complete the handshake.
     *
     * @param clientInit the client's initialisation message
     * @return the server initialisation message to send (framebuffer
     *         dimensions, pixel format, desktop name)
     */
    ServerInit onClientInit(ClientInit clientInit);

    /**
     * Called when the client requests a framebuffer update.
     * This is the primary driver of display refresh — implementations must
     * respond by sending one or more {@link FramebufferUpdate} messages via
     * the {@link VncSocketServerHandle}.
     *
     * @param msg the update request from the client
     */
    void onFramebufferUpdateRequest(FramebufferUpdateRequest msg);

    /**
     * Called when the client specifies the pixel format it wishes to receive.
     * Default implementation does nothing.
     *
     * @param msg the pixel format message
     */
    default void onSetPixelFormat(SetPixelFormat msg) {}

    /**
     * Called when the client lists its supported encodings in preference order.
     * Default implementation does nothing.
     *
     * @param msg the set-encodings message
     */
    default void onSetEncodings(SetEncodings msg) {}

    /**
     * Called when the client sends a key press or release event.
     * Default implementation does nothing.
     *
     * @param msg the key event
     */
    default void onKeyEvent(KeyEvent msg) {}

    /**
     * Called when the client sends a pointer (mouse) event.
     * Default implementation does nothing.
     *
     * @param msg the pointer event
     */
    default void onPointerEvent(PointerEvent msg) {}

    /**
     * Called when the client sends clipboard text.
     * Default implementation does nothing.
     *
     * @param msg the clipboard message
     */
    default void onClientCutText(ClientCutText msg) {}

    /**
     * Called when the connection is closed, whether normally or due to an
     * error. Default implementation does nothing.
     */
    default void onClose() {}
}
