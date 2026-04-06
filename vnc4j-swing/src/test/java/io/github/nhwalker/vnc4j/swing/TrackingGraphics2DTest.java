package io.github.nhwalker.vnc4j.swing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TrackingGraphics2D}: verifies that dirty-region tracking,
 * copy-area events, and solid-fill events are recorded correctly.
 */
class TrackingGraphics2DTest {

    private static final int W = 200;
    private static final int H = 150;

    private BufferedImage image;
    private TrackingGraphics2D g;

    @BeforeEach
    void setUp() {
        image = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D real = image.createGraphics();
        g = new TrackingGraphics2D(real, W, H);
    }

    // -------------------------------------------------------------------------
    // Helper: drain everything cleanly
    // -------------------------------------------------------------------------

    private void drainAll() {
        g.sharedState().drainCopyEvents();
        g.sharedState().drainFillEvents();
        g.sharedState().drainDirtyUnion();
    }

    // -------------------------------------------------------------------------
    // Initial state
    // -------------------------------------------------------------------------

    @Test
    void initialState_noPendingEvents() {
        assertNull(g.sharedState().drainDirtyUnion(), "no dirty region initially");
        assertTrue(g.sharedState().drainCopyEvents().isEmpty(), "no copy events");
        assertTrue(g.sharedState().drainFillEvents().isEmpty(), "no fill events");
    }

    // -------------------------------------------------------------------------
    // Solid fill tracking
    // -------------------------------------------------------------------------

    @Test
    void fillRect_solidColor_recordsFillEvent() {
        g.setColor(Color.RED);
        g.fillRect(10, 20, 50, 30);

        List<TrackingGraphics2D.SolidFillEvent> fills = g.sharedState().drainFillEvents();
        assertEquals(1, fills.size(), "one fill event");
        TrackingGraphics2D.SolidFillEvent e = fills.get(0);
        assertEquals(10, e.x());
        assertEquals(20, e.y());
        assertEquals(50, e.w());
        assertEquals(30, e.h());
        // Color.RED argb ignoring alpha
        assertEquals(Color.RED.getRGB(), e.argb());
    }

    @Test
    void fillRect_solidColor_doesNotExpandDirtyUnion() {
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 10, 10);

        // Fill events are not added to the dirty union — they are separate
        assertNull(g.sharedState().drainDirtyUnion(),
                "solid fillRect should not expand raw dirty union");
    }

    @Test
    void fillRect_afterDrain_stateClears() {
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, 10, 10);

        // First drain
        List<TrackingGraphics2D.SolidFillEvent> fills1 = g.sharedState().drainFillEvents();
        assertEquals(1, fills1.size());

        // Second drain — should be empty
        List<TrackingGraphics2D.SolidFillEvent> fills2 = g.sharedState().drainFillEvents();
        assertTrue(fills2.isEmpty());
    }

    // -------------------------------------------------------------------------
    // copyArea tracking
    // -------------------------------------------------------------------------

    @Test
    void copyArea_recordsCopyEvent() {
        g.copyArea(0, 0, 100, 50, 0, 50); // copy (0,0,100,50) → (0,50,100,50)

        List<TrackingGraphics2D.CopyAreaEvent> copies = g.sharedState().drainCopyEvents();
        assertEquals(1, copies.size());
        TrackingGraphics2D.CopyAreaEvent e = copies.get(0);
        assertEquals(0, e.srcX());
        assertEquals(0, e.srcY());
        assertEquals(0, e.dstX());
        assertEquals(50, e.dstY());
        assertEquals(100, e.w());
        assertEquals(50, e.h());
    }

    @Test
    void copyArea_alsoExpandsDirtyUnionAtDestination() {
        g.copyArea(10, 10, 20, 20, 5, 5); // dst = (15,15,20,20)

        Rectangle dirty = g.sharedState().drainDirtyUnion();
        assertNotNull(dirty, "copyArea destination should mark dirty");
        // Destination rect is (15,15,20,20); dirty should cover it
        assertTrue(dirty.contains(15, 15), "dirty covers dst origin");
        assertTrue(dirty.contains(34, 34), "dirty covers dst extent");
    }

    // -------------------------------------------------------------------------
    // Generic drawing → dirty union
    // -------------------------------------------------------------------------

    @Test
    void drawLine_expandsDirtyUnion() {
        g.setColor(Color.BLACK);
        g.drawLine(5, 5, 50, 50);

        Rectangle dirty = g.sharedState().drainDirtyUnion();
        assertNotNull(dirty);
        assertTrue(dirty.contains(5, 5), "dirty covers start point");
        assertTrue(dirty.contains(50, 50), "dirty covers end point");
    }

    @Test
    void fillRect_withGradientPaint_expandsDirtyUnion() {
        // Non-solid paint → goes to dirty union, not fill events
        g.setPaint(new GradientPaint(0, 0, Color.RED, 100, 0, Color.BLUE));
        g.fillRect(10, 10, 80, 40);

        assertTrue(g.sharedState().drainFillEvents().isEmpty(), "no fill event for gradient");
        Rectangle dirty = g.sharedState().drainDirtyUnion();
        assertNotNull(dirty);
        assertTrue(dirty.contains(10, 10));
        assertTrue(dirty.contains(89, 49));
    }

    @Test
    void multipleDraws_dirtyUnionIsUnion() {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 10, 10);    // fill event, no dirty
        g.setPaint(Color.RED);
        g.draw(new Rectangle(50, 50, 20, 20));  // dirty

        Rectangle dirty = g.sharedState().drainDirtyUnion();
        assertNotNull(dirty, "at least one dirty region");
        // The draw(Rectangle) around (50,50,20,20) must be covered
        assertTrue(dirty.contains(50, 50));
    }

    // -------------------------------------------------------------------------
    // drain clears state
    // -------------------------------------------------------------------------

    @Test
    void drainDirtyUnion_clearsAfterDrain() {
        g.setPaint(Color.BLUE);
        g.draw(new Rectangle(0, 0, 50, 50));

        Rectangle first = g.sharedState().drainDirtyUnion();
        assertNotNull(first);

        Rectangle second = g.sharedState().drainDirtyUnion();
        assertNull(second, "dirty union cleared after drain");
    }

    // -------------------------------------------------------------------------
    // create() shares state
    // -------------------------------------------------------------------------

    @Test
    void childGraphics_sharesTracking() {
        Graphics2D child = (Graphics2D) g.create();
        try {
            child.setColor(Color.MAGENTA);
            child.setPaint(new GradientPaint(0, 0, Color.RED, 10, 0, Color.BLUE));
            child.fillRect(5, 5, 30, 30);
        } finally {
            child.dispose();
        }

        // The child used a gradient → should be in dirty union (not fill events)
        Rectangle dirty = g.sharedState().drainDirtyUnion();
        assertNotNull(dirty, "child drawing is visible in parent's dirty state");
    }

    // -------------------------------------------------------------------------
    // Clip-to-fb: dirty region never exceeds framebuffer bounds
    // -------------------------------------------------------------------------

    @Test
    void fillRect_largerThanFb_clippedToFbBounds() {
        g.setPaint(new GradientPaint(0, 0, Color.RED, 10, 0, Color.BLUE));
        g.fillRect(-10, -10, W + 50, H + 50); // exceeds in all directions

        Rectangle dirty = g.sharedState().drainDirtyUnion();
        assertNotNull(dirty);
        assertTrue(dirty.x >= 0);
        assertTrue(dirty.y >= 0);
        assertTrue(dirty.x + dirty.width <= W);
        assertTrue(dirty.y + dirty.height <= H);
    }

    // -------------------------------------------------------------------------
    // State methods don't mark dirty
    // -------------------------------------------------------------------------

    @Test
    void stateChanges_doNotMarkDirty() {
        g.setColor(Color.RED);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setStroke(new BasicStroke(2.0f));
        g.translate(5, 5);

        assertNull(g.sharedState().drainDirtyUnion(), "state changes must not dirty the framebuffer");
        assertTrue(g.sharedState().drainCopyEvents().isEmpty());
        assertTrue(g.sharedState().drainFillEvents().isEmpty());
    }
}
