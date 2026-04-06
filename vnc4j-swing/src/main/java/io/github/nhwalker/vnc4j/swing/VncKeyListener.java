package io.github.nhwalker.vnc4j.swing;

/**
 * Receives VNC keyboard events forwarded by
 * {@link VirtualFramebufferVncServer}.
 */
@FunctionalInterface
public interface VncKeyListener {

    /**
     * Called when the VNC client presses or releases a key.
     *
     * @param down   {@code true} if the key was pressed, {@code false} if released
     * @param keysym X11 keysym identifying the key (see RFC 6143 §7.5.4)
     */
    void onKey(boolean down, int keysym);
}
