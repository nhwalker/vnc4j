package io.github.nhwalker.vnc4j.swing;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * A virtual framebuffer backed by a {@link BufferedImage}.
 *
 * <p>Application code draws into the framebuffer by calling
 * {@link #createGraphics()}, which returns a tracking {@link Graphics2D}
 * that records what changed. The VNC server thread calls
 * {@link #drainSnapshot()} to atomically collect all pending events and
 * produce the next {@link FrameSnapshot} for encoding.
 *
 * <p>Thread safety: the snapshot drain operation is synchronized so that
 * the event lists are transferred atomically. Pixel data in the underlying
 * image is accessed under {@link #getLock()} by the caller to prevent
 * read/write races during encoding.
 */
public final class VirtualFramebuffer {

    private final BufferedImage image;
    private final TrackingGraphics2D rootGraphics;
    private final Object lock = new Object();

    /**
     * Creates a new framebuffer of the given dimensions.
     * The image type is {@link BufferedImage#TYPE_INT_ARGB}.
     *
     * @param width  framebuffer width in pixels (must be &gt; 0)
     * @param height framebuffer height in pixels (must be &gt; 0)
     */
    public VirtualFramebuffer(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Dimensions must be positive: " + width + "x" + height);
        }
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = this.image.createGraphics();
        this.rootGraphics = new TrackingGraphics2D(g2d, width, height);
    }

    /** Returns the framebuffer width in pixels. */
    public int getWidth() {
        return image.getWidth();
    }

    /** Returns the framebuffer height in pixels. */
    public int getHeight() {
        return image.getHeight();
    }

    /**
     * Creates a new tracking {@link Graphics2D} for drawing into this framebuffer.
     *
     * <p>All drawing operations on the returned context (and any contexts created
     * from it via {@link Graphics2D#create()}) are automatically tracked. The
     * caller is responsible for calling {@link Graphics2D#dispose()} when done.
     *
     * <p>Note: the returned object is NOT thread-safe. Draw from a single thread
     * at a time.
     */
    public Graphics2D createGraphics() {
        return (Graphics2D) rootGraphics.create();
    }

    /**
     * Atomically drains all pending draw events accumulated since the last
     * call and returns a {@link FrameSnapshot} describing what changed.
     *
     * <p>The snapshot's {@link FrameSnapshot#image()} field is the live
     * framebuffer image. Callers that need to read pixel data must do so
     * while holding {@link #getLock()}.
     */
    public FrameSnapshot drainSnapshot() {
        TrackingGraphics2D.SharedState state = rootGraphics.sharedState();
        List<TrackingGraphics2D.CopyAreaEvent> copyEvents = state.drainCopyEvents();
        List<TrackingGraphics2D.SolidFillEvent> fillEvents = state.drainFillEvents();
        Rectangle dirty = state.drainDirtyUnion();
        return new FrameSnapshot(copyEvents, fillEvents, dirty, image);
    }

    /**
     * The lock that must be held while reading pixels from the underlying
     * {@link BufferedImage} returned by a {@link FrameSnapshot}.
     */
    public Object getLock() {
        return lock;
    }

    /**
     * Direct access to the underlying image (for full-frame encoding).
     * Callers must hold {@link #getLock()} while reading.
     */
    BufferedImage getImage() {
        return image;
    }
}
