package io.github.nhwalker.vnc4j.swing;

/**
 * Receives VNC pointer (mouse) events forwarded by
 * {@link VirtualFramebufferVncServer}.
 */
@FunctionalInterface
public interface VncPointerListener {

    /**
     * Called when the VNC client moves the pointer or changes a button state.
     *
     * @param x          pointer X coordinate in framebuffer pixels
     * @param y          pointer Y coordinate in framebuffer pixels
     * @param buttonMask bitmask of pressed buttons (bit 0 = left, bit 1 = middle,
     *                   bit 2 = right, bits 3–7 = scroll/extra)
     */
    void onPointer(int x, int y, int buttonMask);
}
