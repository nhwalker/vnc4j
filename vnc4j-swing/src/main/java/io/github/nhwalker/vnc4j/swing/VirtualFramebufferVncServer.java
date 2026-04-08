package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.*;
import io.github.nhwalker.vnc4j.protocol.messages.*;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A {@link VncServer} that serves a {@link VirtualFramebuffer} over VNC.
 *
 * <p>On each {@link #onFramebufferUpdateRequest} call the server:
 * <ol>
 *   <li>Drains pending events from the framebuffer.</li>
 *   <li>Emits {@link RfbRectangleCopyRect} for any {@code copyArea} operations.</li>
 *   <li>Emits {@link RfbRectangleRre} (0 sub-rects) for solid-colour fill operations.</li>
 *   <li>Emits a single {@link RfbRectangleRaw} for the remaining dirty-union rectangle.</li>
 * </ol>
 * If the request is non-incremental the entire framebuffer is sent as a single
 * {@link RfbRectangleRaw}.
 *
 * <p>Instances are created via {@link #builder()}.
 */
public final class VirtualFramebufferVncServer implements VncServer {

    // -------------------------------------------------------------------------
    // Default pixel format (32 bpp, 24 depth, little-endian BGR0)
    // -------------------------------------------------------------------------

    /**
     * Returns the default pixel format announced by the server: 32 bpp, 24-bit
     * colour depth, little-endian, BGR channel layout (R at shift 16, G at shift 8,
     * B at shift 0).
     */
    public static PixelFormat defaultPixelFormat() {
        return PixelFormat.newBuilder()
                .bitsPerPixel(32)
                .depth(24)
                .bigEndian(false)
                .trueColour(true)
                .redMax(255).redShift(16)
                .greenMax(255).greenShift(8)
                .blueMax(255).blueShift(0)
                .build();
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final VncSocketServerHandle handle;
    private final VirtualFramebuffer framebuffer;
    private final String desktopName;
    private final VncPointerListener pointerListener;
    private final VncKeyListener keyListener;

    /** Negotiated pixel format; may be updated by SetPixelFormat. */
    private volatile PixelFormat pixelFormat;

    // -------------------------------------------------------------------------
    // Constructor (package-private — use builder)
    // -------------------------------------------------------------------------

    private VirtualFramebufferVncServer(Builder builder) {
        this.handle = Objects.requireNonNull(builder.handle, "handle");
        this.framebuffer = Objects.requireNonNull(builder.framebuffer, "framebuffer");
        this.desktopName = (builder.desktopName != null) ? builder.desktopName : "vnc4j";
        this.pixelFormat = (builder.pixelFormat != null) ? builder.pixelFormat : defaultPixelFormat();
        this.pointerListener = builder.pointerListener;
        this.keyListener = builder.keyListener;
    }

    // -------------------------------------------------------------------------
    // VncServer implementation
    // -------------------------------------------------------------------------

    @Override
    public ServerInit onClientInit(ClientInit clientInit) {
        return ServerInit.newBuilder()
                .framebufferWidth(framebuffer.getWidth())
                .framebufferHeight(framebuffer.getHeight())
                .pixelFormat(pixelFormat)
                .name(desktopName)
                .build();
    }

    @Override
    public void onSetPixelFormat(SetPixelFormat msg) {
        pixelFormat = msg.pixelFormat();
    }

    @Override
    public void onFramebufferUpdateRequest(FramebufferUpdateRequest msg) {
        try {
            if (!msg.incremental()) {
                sendFullFrame();
            } else {
                sendIncrementalUpdate();
            }
        } catch (IOException e) {
            // Connection likely closed; close our end too.
            try { handle.close(); } catch (IOException ignored) {}
        }
    }

    @Override
    public void onPointerEvent(PointerEvent msg) {
        if (pointerListener != null) {
            pointerListener.onPointer(msg.x(), msg.y(), msg.buttonMask());
        }
    }

    @Override
    public void onKeyEvent(KeyEvent msg) {
        if (keyListener != null) {
            keyListener.onKey(msg.down(), msg.key());
        }
    }

    // -------------------------------------------------------------------------
    // Frame encoding
    // -------------------------------------------------------------------------

    /** Sends the entire framebuffer as a single Raw rectangle. */
    private void sendFullFrame() throws IOException {
        int w = framebuffer.getWidth();
        int h = framebuffer.getHeight();
        byte[] pixels;
        synchronized (framebuffer.getLock()) {
            pixels = PixelEncoder.encodeRegion(framebuffer.getImage(), 0, 0, w, h, pixelFormat);
        }
        RfbRectangleRaw raw = RfbRectangleRaw.newBuilder()
                .x(0).y(0).width(w).height(h)
                .pixels(pixels)
                .build();
        handle.send(FramebufferUpdate.newBuilder()
                .rectangles(List.of(raw))
                .build());
    }

    /** Drains pending events and sends a minimal incremental update. */
    private void sendIncrementalUpdate() throws IOException {
        FrameSnapshot snap = framebuffer.drainSnapshot();
        PixelFormat fmt = pixelFormat; // capture volatile once

        List<RfbRectangle> rects = new ArrayList<>();

        // 1. CopyRect rectangles FIRST — must precede any Raw updates that
        //    would overwrite the source regions still in the client framebuffer.
        for (TrackingGraphics2D.CopyAreaEvent e : snap.copyEvents()) {
            rects.add(RfbRectangleCopyRect.newBuilder()
                    .x(e.dstX()).y(e.dstY()).width(e.w()).height(e.h())
                    .srcX(e.srcX()).srcY(e.srcY())
                    .build());
        }

        // 2. Solid-fill rectangles as RRE with 0 sub-rects.
        for (TrackingGraphics2D.SolidFillEvent e : snap.fillEvents()) {
            byte[] bg = PixelEncoder.encodePixel(e.argb(), fmt);
            rects.add(RfbRectangleRre.newBuilder()
                    .x(e.x()).y(e.y()).width(e.w()).height(e.h())
                    .background(bg)
                    .subrects(List.of())
                    .build());
        }

        // 3. Remaining dirty region as Raw.
        Rectangle dirty = snap.dirtyUnion();
        if (dirty != null && !dirty.isEmpty()) {
            byte[] pixels;
            synchronized (framebuffer.getLock()) {
                pixels = PixelEncoder.encodeRegion(
                        snap.image(), dirty.x, dirty.y, dirty.width, dirty.height, fmt);
            }
            rects.add(RfbRectangleRaw.newBuilder()
                    .x(dirty.x).y(dirty.y).width(dirty.width).height(dirty.height)
                    .pixels(pixels)
                    .build());
        }

        if (!rects.isEmpty()) {
            handle.send(FramebufferUpdate.newBuilder()
                    .rectangles(rects)
                    .build());
        }
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    /** Creates a new {@link Builder}. */
    public static Builder builder() {
        return new Builder();
    }

    /** Fluent builder for {@link VirtualFramebufferVncServer}. */
    public static final class Builder {

        private VncSocketServerHandle handle;
        private VirtualFramebuffer framebuffer;
        private PixelFormat pixelFormat;
        private String desktopName;
        private VncPointerListener pointerListener;
        private VncKeyListener keyListener;

        private Builder() {}

        /**
         * Sets the {@link VncSocketServerHandle} used to send messages to the client.
         * Required.
         */
        public Builder handle(VncSocketServerHandle handle) {
            this.handle = handle;
            return this;
        }

        /**
         * Sets the {@link VirtualFramebuffer} that will be served.
         * Required.
         */
        public Builder framebuffer(VirtualFramebuffer framebuffer) {
            this.framebuffer = framebuffer;
            return this;
        }

        /**
         * Overrides the pixel format sent in {@code ServerInit} and used for encoding.
         * Defaults to {@link VirtualFramebufferVncServer#defaultPixelFormat()}.
         */
        public Builder pixelFormat(PixelFormat pixelFormat) {
            this.pixelFormat = pixelFormat;
            return this;
        }

        /**
         * Sets the desktop name sent in {@code ServerInit}.
         * Defaults to {@code "vnc4j"}.
         */
        public Builder desktopName(String desktopName) {
            this.desktopName = desktopName;
            return this;
        }

        /**
         * Registers a listener for pointer (mouse) events from the client.
         * Optional.
         */
        public Builder pointerListener(VncPointerListener pointerListener) {
            this.pointerListener = pointerListener;
            return this;
        }

        /**
         * Registers a listener for key events from the client.
         * Optional.
         */
        public Builder keyListener(VncKeyListener keyListener) {
            this.keyListener = keyListener;
            return this;
        }

        /** Builds a new {@link VirtualFramebufferVncServer}. */
        public VirtualFramebufferVncServer build() {
            return new VirtualFramebufferVncServer(this);
        }
    }
}
