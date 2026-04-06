package io.github.nhwalker.vnc4j.swing;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A {@link Graphics2D} wrapper that intercepts all drawing operations and
 * records them as events suitable for generating efficient VNC framebuffer
 * updates.
 *
 * <p>All drawing is forwarded to a delegate {@link Graphics2D} that paints
 * into the actual {@link java.awt.image.BufferedImage}. In parallel:
 * <ul>
 *   <li>{@link #copyArea} calls are recorded as {@link CopyAreaEvent} objects
 *       for encoding as {@code RfbRectangleCopyRect}.</li>
 *   <li>{@link #fillRect}/{@link #clearRect} calls with a solid {@link Color}
 *       paint and simple composite are recorded as {@link SolidFillEvent}
 *       objects for encoding as {@code RfbRectangleRre} (0 sub-rects).</li>
 *   <li>All other drawing operations expand a dirty-union rectangle, which
 *       is sent as {@code RfbRectangleRaw} on the next update cycle.</li>
 * </ul>
 *
 * <p>Instances created via {@link #create()} share the same {@link SharedState}
 * so that drawing from child graphics contexts is also tracked. The shared
 * state is thread-safe: drawing threads and the VNC server thread may call
 * drain methods concurrently.
 */
final class TrackingGraphics2D extends Graphics2D {

    // -------------------------------------------------------------------------
    // Event types
    // -------------------------------------------------------------------------

    /** Records a {@code copyArea} call for encoding as CopyRect. */
    record CopyAreaEvent(int srcX, int srcY, int dstX, int dstY, int w, int h) {}

    /**
     * Records a solid-colour {@code fillRect}/{@code clearRect} call for
     * encoding as RRE with 0 sub-rectangles. Coordinates are in device space.
     */
    record SolidFillEvent(int x, int y, int w, int h, int argb) {}

    // -------------------------------------------------------------------------
    // Shared mutable state (shared by all create() copies)
    // -------------------------------------------------------------------------

    static final class SharedState {
        private final int fbWidth;
        private final int fbHeight;
        private final List<CopyAreaEvent> copyEvents = new ArrayList<>();
        private final List<SolidFillEvent> fillEvents = new ArrayList<>();
        private Rectangle dirtyUnion = null;

        SharedState(int fbWidth, int fbHeight) {
            this.fbWidth = fbWidth;
            this.fbHeight = fbHeight;
        }

        synchronized void expandDirty(Rectangle r) {
            if (r == null || r.isEmpty()) return;
            dirtyUnion = (dirtyUnion == null) ? new Rectangle(r) : dirtyUnion.union(r);
        }

        synchronized void addCopyEvent(CopyAreaEvent e) {
            copyEvents.add(e);
        }

        synchronized void addFillEvent(SolidFillEvent e) {
            fillEvents.add(e);
        }

        synchronized List<CopyAreaEvent> drainCopyEvents() {
            if (copyEvents.isEmpty()) return List.of();
            List<CopyAreaEvent> result = new ArrayList<>(copyEvents);
            copyEvents.clear();
            return result;
        }

        synchronized List<SolidFillEvent> drainFillEvents() {
            if (fillEvents.isEmpty()) return List.of();
            List<SolidFillEvent> result = new ArrayList<>(fillEvents);
            fillEvents.clear();
            return result;
        }

        synchronized Rectangle drainDirtyUnion() {
            Rectangle r = dirtyUnion;
            dirtyUnion = null;
            return r;
        }
    }

    // -------------------------------------------------------------------------
    // Instance fields
    // -------------------------------------------------------------------------

    private final Graphics2D delegate;
    private final SharedState state;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Creates the root tracking wrapper. */
    TrackingGraphics2D(Graphics2D delegate, int fbWidth, int fbHeight) {
        this.delegate = delegate;
        this.state = new SharedState(fbWidth, fbHeight);
    }

    /** Creates a child wrapper sharing the parent's state (used by create()). */
    private TrackingGraphics2D(Graphics2D delegate, SharedState state) {
        this.delegate = delegate;
        this.state = state;
    }

    // -------------------------------------------------------------------------
    // Drain accessors (called by VirtualFramebuffer)
    // -------------------------------------------------------------------------

    SharedState sharedState() { return state; }

    // -------------------------------------------------------------------------
    // Dirty-region helpers
    // -------------------------------------------------------------------------

    /**
     * Computes the device-space bounding rectangle for a user-space axis-aligned
     * rectangle (x, y, w, h). Clips the result to the framebuffer bounds.
     */
    private Rectangle deviceBoundsOf(double x, double y, double w, double h) {
        AffineTransform t = delegate.getTransform();
        double[] corners = { x, y, x + w, y, x + w, y + h, x, y + h };
        t.transform(corners, 0, corners, 0, 4);
        double minX = corners[0], maxX = corners[0];
        double minY = corners[1], maxY = corners[1];
        for (int i = 2; i < 8; i += 2) {
            if (corners[i] < minX) minX = corners[i];
            if (corners[i] > maxX) maxX = corners[i];
            if (corners[i + 1] < minY) minY = corners[i + 1];
            if (corners[i + 1] > maxY) maxY = corners[i + 1];
        }
        return clipToFb((int) Math.floor(minX), (int) Math.floor(minY),
                (int) Math.ceil(maxX - Math.floor(minX)),
                (int) Math.ceil(maxY - Math.floor(minY)));
    }

    private Rectangle deviceBoundsOfShape(Shape s) {
        if (s == null) return null;
        AffineTransform t = delegate.getTransform();
        Rectangle2D b = t.createTransformedShape(s).getBounds2D();
        return clipToFb((int) Math.floor(b.getX()), (int) Math.floor(b.getY()),
                (int) Math.ceil(b.getWidth()) + 1, (int) Math.ceil(b.getHeight()) + 1);
    }

    private Rectangle deviceBoundsOfStrokedShape(Shape s) {
        if (s == null) return null;
        Stroke stroke = delegate.getStroke();
        Shape stroked = (stroke != null) ? stroke.createStrokedShape(s) : s;
        return deviceBoundsOfShape(stroked);
    }

    private Rectangle clipToFb(int x, int y, int w, int h) {
        int x2 = Math.min(x + w, state.fbWidth);
        int y2 = Math.min(y + h, state.fbHeight);
        int cx = Math.max(x, 0);
        int cy = Math.max(y, 0);
        return new Rectangle(cx, cy, Math.max(x2 - cx, 0), Math.max(y2 - cy, 0));
    }

    /**
     * Expands the dirty union by the device-space bounding box of the given
     * user-space axis-aligned rectangle.
     */
    private void markDirty(double x, double y, double w, double h) {
        state.expandDirty(deviceBoundsOf(x, y, w, h));
    }

    private void markDirtyShape(Shape s) {
        state.expandDirty(deviceBoundsOfShape(s));
    }

    private void markDirtyStrokedShape(Shape s) {
        state.expandDirty(deviceBoundsOfStrokedShape(s));
    }

    /**
     * Returns true when the current transform is a pure translation (possibly
     * identity), making fillRect safe to encode as an axis-aligned device rect.
     */
    private boolean isTranslationOnlyTransform() {
        int type = delegate.getTransform().getType();
        return (type & ~AffineTransform.TYPE_TRANSLATION) == 0;
    }

    /** Returns true when the composite is the default opaque SrcOver. */
    private boolean isSimpleComposite() {
        Composite c = delegate.getComposite();
        return (c instanceof AlphaComposite ac)
                && ac.getRule() == AlphaComposite.SRC_OVER
                && ac.getAlpha() == 1.0f;
    }

    // -------------------------------------------------------------------------
    // create / dispose
    // -------------------------------------------------------------------------

    @Override
    public Graphics create() {
        return new TrackingGraphics2D((Graphics2D) delegate.create(), state);
    }

    @Override
    public void dispose() {
        delegate.dispose();
    }

    // -------------------------------------------------------------------------
    // copyArea — maps to RfbRectangleCopyRect
    // -------------------------------------------------------------------------

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        delegate.copyArea(x, y, width, height, dx, dy);
        // copyArea operates in device coordinates (ignores current transform).
        int dstX = x + dx;
        int dstY = y + dy;
        state.addCopyEvent(new CopyAreaEvent(x, y, dstX, dstY, width, height));
        // Mark destination as raw-dirty so it's covered in case CopyRect is
        // not supported by the negotiated encoding set.
        state.expandDirty(clipToFb(dstX, dstY, width, height));
    }

    // -------------------------------------------------------------------------
    // fillRect / clearRect — may map to RfbRectangleRre
    // -------------------------------------------------------------------------

    @Override
    public void fillRect(int x, int y, int width, int height) {
        delegate.fillRect(x, y, width, height);
        if (delegate.getPaint() instanceof Color c
                && isSimpleComposite()
                && isTranslationOnlyTransform()) {
            AffineTransform t = delegate.getTransform();
            int devX = (int) Math.round(x + t.getTranslateX());
            int devY = (int) Math.round(y + t.getTranslateY());
            Rectangle clipped = clipToFb(devX, devY, width, height);
            if (!clipped.isEmpty()) {
                state.addFillEvent(new SolidFillEvent(
                        clipped.x, clipped.y, clipped.width, clipped.height, c.getRGB()));
            }
        } else {
            markDirty(x, y, width, height);
        }
    }

    @Override
    public void clearRect(int x, int y, int width, int height) {
        delegate.clearRect(x, y, width, height);
        Color bg = delegate.getBackground();
        if (bg != null && isTranslationOnlyTransform()) {
            AffineTransform t = delegate.getTransform();
            int devX = (int) Math.round(x + t.getTranslateX());
            int devY = (int) Math.round(y + t.getTranslateY());
            Rectangle clipped = clipToFb(devX, devY, width, height);
            if (!clipped.isEmpty()) {
                state.addFillEvent(new SolidFillEvent(
                        clipped.x, clipped.y, clipped.width, clipped.height, bg.getRGB()));
            }
        } else {
            markDirty(x, y, width, height);
        }
    }

    // -------------------------------------------------------------------------
    // Primitive drawing — all map to raw dirty union
    // -------------------------------------------------------------------------

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        delegate.drawLine(x1, y1, x2, y2);
        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        markDirtyStrokedShape(new Rectangle(minX, minY,
                Math.abs(x2 - x1) + 1, Math.abs(y2 - y1) + 1));
    }

    @Override
    public void drawRect(int x, int y, int width, int height) {
        delegate.drawRect(x, y, width, height);
        markDirtyStrokedShape(new Rectangle(x, y, width, height));
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        delegate.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
        markDirty(x, y, width, height);
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        delegate.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
        markDirty(x, y, width, height);
    }

    @Override
    public void drawOval(int x, int y, int width, int height) {
        delegate.drawOval(x, y, width, height);
        markDirtyStrokedShape(new Rectangle(x, y, width, height));
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
        delegate.fillOval(x, y, width, height);
        markDirty(x, y, width, height);
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        delegate.drawArc(x, y, width, height, startAngle, arcAngle);
        markDirty(x, y, width, height);
    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        delegate.fillArc(x, y, width, height, startAngle, arcAngle);
        markDirty(x, y, width, height);
    }

    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        delegate.drawPolyline(xPoints, yPoints, nPoints);
        markDirtyStrokedShape(polyBounds(xPoints, yPoints, nPoints));
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        delegate.drawPolygon(xPoints, yPoints, nPoints);
        markDirtyStrokedShape(polyBounds(xPoints, yPoints, nPoints));
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        delegate.fillPolygon(xPoints, yPoints, nPoints);
        markDirtyShape(polyBounds(xPoints, yPoints, nPoints));
    }

    private static Polygon polyBounds(int[] xPoints, int[] yPoints, int nPoints) {
        return new Polygon(xPoints, yPoints, nPoints);
    }

    // -------------------------------------------------------------------------
    // draw / fill (Shape) — Graphics2D abstract
    // -------------------------------------------------------------------------

    @Override
    public void draw(Shape s) {
        delegate.draw(s);
        markDirtyStrokedShape(s);
    }

    @Override
    public void fill(Shape s) {
        delegate.fill(s);
        markDirtyShape(s);
    }

    // -------------------------------------------------------------------------
    // Text
    // -------------------------------------------------------------------------

    @Override
    public void drawString(String str, int x, int y) {
        delegate.drawString(str, x, y);
        markDirtyText(str, x, y);
    }

    @Override
    public void drawString(String str, float x, float y) {
        delegate.drawString(str, x, y);
        markDirtyText(str, (int) Math.floor(x), (int) Math.floor(y));
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        delegate.drawString(iterator, x, y);
        markDirtyTextIterator(x, y);
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        delegate.drawString(iterator, x, y);
        markDirtyTextIterator((int) Math.floor(x), (int) Math.floor(y));
    }

    @Override
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        delegate.drawGlyphVector(g, x, y);
        Rectangle2D vb = g.getVisualBounds();
        markDirty(x + vb.getX() - 1, y + vb.getY() - 1, vb.getWidth() + 2, vb.getHeight() + 2);
    }

    private void markDirtyText(String str, int x, int y) {
        FontMetrics fm = delegate.getFontMetrics();
        int w = fm.stringWidth(str);
        int h = fm.getHeight();
        markDirty(x, y - fm.getAscent(), w + 1, h + 1);
    }

    private void markDirtyTextIterator(int x, int y) {
        FontMetrics fm = delegate.getFontMetrics();
        int h = fm.getHeight();
        // Width is unknown without iterating; use a conservative estimate relative to fb.
        markDirty(x, y - fm.getAscent(), state.fbWidth - x, h + 1);
    }

    // -------------------------------------------------------------------------
    // Images
    // -------------------------------------------------------------------------

    @Override
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        boolean r = delegate.drawImage(img, x, y, observer);
        markDirty(x, y, img.getWidth(observer), img.getHeight(observer));
        return r;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        boolean r = delegate.drawImage(img, x, y, width, height, observer);
        markDirty(x, y, width, height);
        return r;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        boolean r = delegate.drawImage(img, x, y, bgcolor, observer);
        markDirty(x, y, img.getWidth(observer), img.getHeight(observer));
        return r;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height,
            Color bgcolor, ImageObserver observer) {
        boolean r = delegate.drawImage(img, x, y, width, height, bgcolor, observer);
        markDirty(x, y, width, height);
        return r;
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        boolean r = delegate.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
        markDirty(Math.min(dx1, dx2), Math.min(dy1, dy2),
                Math.abs(dx2 - dx1), Math.abs(dy2 - dy1));
        return r;
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        boolean r = delegate.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
        markDirty(Math.min(dx1, dx2), Math.min(dy1, dy2),
                Math.abs(dx2 - dx1), Math.abs(dy2 - dy1));
        return r;
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        boolean r = delegate.drawImage(img, xform, obs);
        // Transform the image bounds through both xform and the current transform.
        double w = img.getWidth(obs);
        double h = img.getHeight(obs);
        double[] corners = { 0, 0, w, 0, w, h, 0, h };
        xform.transform(corners, 0, corners, 0, 4);
        double minX = corners[0], maxX = corners[0];
        double minY = corners[1], maxY = corners[1];
        for (int i = 2; i < 8; i += 2) {
            if (corners[i] < minX) minX = corners[i];
            if (corners[i] > maxX) maxX = corners[i];
            if (corners[i + 1] < minY) minY = corners[i + 1];
            if (corners[i + 1] > maxY) maxY = corners[i + 1];
        }
        markDirty(minX, minY, maxX - minX, maxY - minY);
        return r;
    }

    @Override
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        delegate.drawImage(img, op, x, y);
        markDirty(x, y, img.getWidth(), img.getHeight());
    }

    @Override
    public void drawRenderedImage(java.awt.image.RenderedImage img, AffineTransform xform) {
        delegate.drawRenderedImage(img, xform);
        Rectangle2D b = xform.createTransformedShape(
                new Rectangle(img.getMinX(), img.getMinY(), img.getWidth(), img.getHeight()))
                .getBounds2D();
        markDirty(b.getX(), b.getY(), b.getWidth(), b.getHeight());
    }

    @Override
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        delegate.drawRenderableImage(img, xform);
        Rectangle2D b = xform.createTransformedShape(
                new Rectangle2D.Float(img.getMinX(), img.getMinY(), img.getWidth(), img.getHeight()))
                .getBounds2D();
        markDirty(b.getX(), b.getY(), b.getWidth(), b.getHeight());
    }

    // -------------------------------------------------------------------------
    // State — pure delegation, no dirty effect
    // -------------------------------------------------------------------------

    @Override public Color getColor() { return delegate.getColor(); }
    @Override public void setColor(Color c) { delegate.setColor(c); }
    @Override public void setPaintMode() { delegate.setPaintMode(); }
    @Override public void setXORMode(Color c1) { delegate.setXORMode(c1); }
    @Override public Font getFont() { return delegate.getFont(); }
    @Override public void setFont(Font font) { delegate.setFont(font); }
    @Override public FontMetrics getFontMetrics(Font f) { return delegate.getFontMetrics(f); }

    @Override public void setComposite(Composite comp) { delegate.setComposite(comp); }
    @Override public void setPaint(Paint paint) { delegate.setPaint(paint); }
    @Override public void setStroke(Stroke s) { delegate.setStroke(s); }
    @Override public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
        delegate.setRenderingHint(hintKey, hintValue);
    }
    @Override public void setRenderingHints(Map<?, ?> hints) { delegate.setRenderingHints(hints); }
    @Override public void addRenderingHints(Map<?, ?> hints) { delegate.addRenderingHints(hints); }
    @Override public Object getRenderingHint(RenderingHints.Key hintKey) {
        return delegate.getRenderingHint(hintKey);
    }
    @Override public RenderingHints getRenderingHints() { return delegate.getRenderingHints(); }
    @Override public Paint getPaint() { return delegate.getPaint(); }
    @Override public Composite getComposite() { return delegate.getComposite(); }
    @Override public void setBackground(Color color) { delegate.setBackground(color); }
    @Override public Color getBackground() { return delegate.getBackground(); }
    @Override public Stroke getStroke() { return delegate.getStroke(); }
    @Override public FontRenderContext getFontRenderContext() { return delegate.getFontRenderContext(); }
    @Override public GraphicsConfiguration getDeviceConfiguration() { return delegate.getDeviceConfiguration(); }

    // -------------------------------------------------------------------------
    // Transform — pure delegation
    // -------------------------------------------------------------------------

    @Override public void translate(int x, int y) { delegate.translate(x, y); }
    @Override public void translate(double tx, double ty) { delegate.translate(tx, ty); }
    @Override public void rotate(double theta) { delegate.rotate(theta); }
    @Override public void rotate(double theta, double x, double y) { delegate.rotate(theta, x, y); }
    @Override public void scale(double sx, double sy) { delegate.scale(sx, sy); }
    @Override public void shear(double shx, double shy) { delegate.shear(shx, shy); }
    @Override public void transform(AffineTransform Tx) { delegate.transform(Tx); }
    @Override public void setTransform(AffineTransform Tx) { delegate.setTransform(Tx); }
    @Override public AffineTransform getTransform() { return delegate.getTransform(); }

    // -------------------------------------------------------------------------
    // Clip — pure delegation
    // -------------------------------------------------------------------------

    @Override public Rectangle getClipBounds() { return delegate.getClipBounds(); }
    @Override public void clipRect(int x, int y, int width, int height) {
        delegate.clipRect(x, y, width, height);
    }
    @Override public void setClip(int x, int y, int width, int height) {
        delegate.setClip(x, y, width, height);
    }
    @Override public Shape getClip() { return delegate.getClip(); }
    @Override public void setClip(Shape clip) { delegate.setClip(clip); }
    @Override public void clip(Shape s) { delegate.clip(s); }

    // -------------------------------------------------------------------------
    // Misc
    // -------------------------------------------------------------------------

    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        return delegate.hit(rect, s, onStroke);
    }

    @Override
    public void draw3DRect(int x, int y, int width, int height, boolean raised) {
        delegate.draw3DRect(x, y, width, height, raised);
        markDirty(x, y, width + 1, height + 1);
    }

    @Override
    public void fill3DRect(int x, int y, int width, int height, boolean raised) {
        delegate.fill3DRect(x, y, width, height, raised);
        markDirty(x, y, width, height);
    }

}
