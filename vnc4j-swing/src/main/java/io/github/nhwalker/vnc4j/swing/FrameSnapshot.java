package io.github.nhwalker.vnc4j.swing;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * An immutable snapshot of pending dirty-state drained from a
 * {@link VirtualFramebuffer} at a single point in time.
 *
 * <p>Passed from {@link VirtualFramebuffer#drainSnapshot()} to
 * {@link VirtualFramebufferVncServer} to drive a single {@code FramebufferUpdate}.
 *
 * <p>{@code image} is the live {@link BufferedImage} — callers must hold
 * {@link VirtualFramebuffer#getLock()} while reading pixels from it.
 */
record FrameSnapshot(
        List<TrackingGraphics2D.CopyAreaEvent> copyEvents,
        List<TrackingGraphics2D.SolidFillEvent> fillEvents,
        Rectangle dirtyUnion,
        BufferedImage image
) {}
