package io.github.nhwalker.vnc4j.swing;

import io.github.nhwalker.vnc4j.protocol.FramebufferUpdate;
import io.github.nhwalker.vnc4j.protocol.FramebufferUpdateRequest;
import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.PointerEvent;
import io.github.nhwalker.vnc4j.protocol.RfbRectangle;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleCopyRect;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleCoRre;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleCursor;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleCursorWithAlpha;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleDesktopSize;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleExtendedDesktopSize;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleH264;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleHextile;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleJpeg;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleLastRect;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleRaw;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleRre;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleTightBasic;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleTightFill;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleTightJpeg;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleTightPngFill;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleTightPngJpeg;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleTightPngPng;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleXCursor;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleZlib;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleZlibHex;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleZrle;
import io.github.nhwalker.vnc4j.protocol.ServerInit;
import io.github.nhwalker.vnc4j.protocol.SetEncodings;
import io.github.nhwalker.vnc4j.protocol.VncClient;
import io.github.nhwalker.vnc4j.protocol.VncClientFactory;
import io.github.nhwalker.vnc4j.protocol.VncSocketClientHandle;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Swing panel that displays a live VNC remote desktop.
 *
 * <p>{@code VncPanel} implements {@link VncClientFactory} so it can be passed
 * directly to {@link io.github.nhwalker.vnc4j.protocol.VncSocketClient}:
 *
 * <pre>{@code
 * VncPanel panel = new VncPanel();
 * frame.add(new JScrollPane(panel));
 * VncSocketClient client = new VncSocketClient("host", 5900, panel);
 * client.start();
 * // To disconnect: client.close()
 * }</pre>
 *
 * <p>The panel paints the remote framebuffer, forwards mouse and keyboard input
 * to the server, and handles cursor-shape and desktop-resize pseudo-encodings.
 */
public class VncPanel extends JPanel implements VncClientFactory {

    // Encoding type IDs advertised to the server.
    // JPEG (type 21) is excluded because its renderer throws UnsupportedOperationException.
    private static final List<Integer> SUPPORTED_ENCODINGS = List.of(
            0,     // Raw
            1,     // CopyRect
            2,     // RRE
            4,     // CoRRE
            5,     // Hextile
            6,     // Zlib
            7,     // Tight
            8,     // ZlibHex
            16,    // ZRLE
            23,    // H264
            -260,  // TightPng
            -239,  // Cursor
            -240,  // XCursor
            -314,  // CursorWithAlpha
            -223,  // DesktopSize
            -308,  // ExtendedDesktopSize
            -224   // LastRect
    );

    // AWT VK_ code → X11 keysym for non-printable / special keys.
    private static final Map<Integer, Integer> SPECIAL_KEYS;
    static {
        Map<Integer, Integer> m = new HashMap<>();
        m.put(KeyEvent.VK_BACK_SPACE,  0xFF08);
        m.put(KeyEvent.VK_TAB,         0xFF09);
        m.put(KeyEvent.VK_ENTER,       0xFF0D);
        m.put(KeyEvent.VK_ESCAPE,      0xFF1B);
        m.put(KeyEvent.VK_DELETE,      0xFFFF);
        m.put(KeyEvent.VK_HOME,        0xFF50);
        m.put(KeyEvent.VK_LEFT,        0xFF51);
        m.put(KeyEvent.VK_UP,          0xFF52);
        m.put(KeyEvent.VK_RIGHT,       0xFF53);
        m.put(KeyEvent.VK_DOWN,        0xFF54);
        m.put(KeyEvent.VK_PAGE_UP,     0xFF55);
        m.put(KeyEvent.VK_PAGE_DOWN,   0xFF56);
        m.put(KeyEvent.VK_END,         0xFF57);
        m.put(KeyEvent.VK_INSERT,      0xFF63);
        m.put(KeyEvent.VK_F1,          0xFFBE);
        m.put(KeyEvent.VK_F2,          0xFFBF);
        m.put(KeyEvent.VK_F3,          0xFFC0);
        m.put(KeyEvent.VK_F4,          0xFFC1);
        m.put(KeyEvent.VK_F5,          0xFFC2);
        m.put(KeyEvent.VK_F6,          0xFFC3);
        m.put(KeyEvent.VK_F7,          0xFFC4);
        m.put(KeyEvent.VK_F8,          0xFFC5);
        m.put(KeyEvent.VK_F9,          0xFFC6);
        m.put(KeyEvent.VK_F10,         0xFFC7);
        m.put(KeyEvent.VK_F11,         0xFFC8);
        m.put(KeyEvent.VK_F12,         0xFFC9);
        m.put(KeyEvent.VK_SHIFT,       0xFFE1);
        m.put(KeyEvent.VK_CONTROL,     0xFFE3);
        m.put(KeyEvent.VK_ALT,         0xFFE9);
        m.put(KeyEvent.VK_META,        0xFFE7);
        m.put(KeyEvent.VK_CAPS_LOCK,   0xFFE5);
        m.put(KeyEvent.VK_NUM_LOCK,    0xFF7F);
        m.put(KeyEvent.VK_SCROLL_LOCK, 0xFF14);
        m.put(KeyEvent.VK_PRINTSCREEN, 0xFF61);
        m.put(KeyEvent.VK_PAUSE,       0xFF13);
        SPECIAL_KEYS = Map.copyOf(m);
    }

    private final Object framebufferLock = new Object();
    private BufferedImage framebuffer; // guarded by framebufferLock

    private volatile VncSocketClientHandle activeHandle;
    private int currentButtonMask; // EDT only

    /**
     * Creates a {@code VncPanel} with a black background, ready to accept
     * connections via {@link #create(VncSocketClientHandle)}.
     */
    public VncPanel() {
        setFocusable(true);
        setBackground(Color.BLACK);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                currentButtonMask |= buttonBit(e.getButton());
                sendPointerEvent(e.getX(), e.getY(), currentButtonMask);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                currentButtonMask &= ~buttonBit(e.getButton());
                sendPointerEvent(e.getX(), e.getY(), currentButtonMask);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                sendPointerEvent(e.getX(), e.getY(), currentButtonMask);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                sendPointerEvent(e.getX(), e.getY(), currentButtonMask);
            }
        });

        addMouseWheelListener((MouseWheelEvent e) -> {
            // Bits 3 and 4 represent scroll-up and scroll-down in the RFB button mask.
            int scrollBit = e.getWheelRotation() < 0 ? (1 << 3) : (1 << 4);
            int x = e.getX(), y = e.getY();
            int notches = Math.abs(e.getWheelRotation());
            for (int i = 0; i < notches; i++) {
                sendPointerEvent(x, y, currentButtonMask | scrollBit);
                sendPointerEvent(x, y, currentButtonMask);
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int ks = toX11Keysym(e);
                if (ks != 0) sendKeyEvent(true, ks);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int ks = toX11Keysym(e);
                if (ks != 0) sendKeyEvent(false, ks);
            }
        });
    }

    // -------------------------------------------------------------------------
    // VncClientFactory
    // -------------------------------------------------------------------------

    /**
     * Creates a new {@link VncClient} for an inbound connection. Called by
     * {@link io.github.nhwalker.vnc4j.protocol.VncSocketClient} on each connection;
     * the returned client drives rendering and input for the lifetime of that
     * connection.
     */
    @Override
    public VncClient create(VncSocketClientHandle handle) {
        activeHandle = handle;
        return new ConnectionClient(handle);
    }

    // -------------------------------------------------------------------------
    // Painting
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage fb;
        synchronized (framebufferLock) {
            fb = framebuffer;
        }
        if (fb != null) {
            g.drawImage(fb, 0, 0, null);
        }
    }

    // -------------------------------------------------------------------------
    // Input helpers (called from EDT via listeners)
    // -------------------------------------------------------------------------

    private void sendPointerEvent(int x, int y, int buttonMask) {
        VncSocketClientHandle h = activeHandle;
        if (h == null) return;
        try {
            h.send(PointerEvent.newBuilder()
                    .x(x).y(y)
                    .buttonMask(buttonMask)
                    .build());
        } catch (IOException ignored) {}
    }

    private void sendKeyEvent(boolean down, int keysym) {
        VncSocketClientHandle h = activeHandle;
        if (h == null) return;
        try {
            h.send(io.github.nhwalker.vnc4j.protocol.KeyEvent.newBuilder()
                    .down(down)
                    .key(keysym)
                    .build());
        } catch (IOException ignored) {}
    }

    /**
     * Maps an AWT mouse button number (1=left, 2=middle, 3=right) to the
     * corresponding RFB button-mask bit position.
     */
    private static int buttonBit(int awtButton) {
        return switch (awtButton) {
            case MouseEvent.BUTTON1 -> 1;
            case MouseEvent.BUTTON2 -> 1 << 1;
            case MouseEvent.BUTTON3 -> 1 << 2;
            default -> 0;
        };
    }

    /**
     * Maps an AWT {@link KeyEvent} to an X11 keysym value.
     *
     * <p>Letter keys are mapped to their lowercase keysym so that the server
     * combines them with any shift state sent separately. Other printable keys
     * whose VK_ code equals their Latin-1 value are passed through directly.
     * Special keys (arrows, function keys, modifiers, etc.) are looked up in
     * {@link #SPECIAL_KEYS}.
     *
     * @return the X11 keysym, or {@code 0} if unmappable
     */
    private static int toX11Keysym(KeyEvent e) {
        int vk = e.getKeyCode();

        // Letter keys: VK_A–VK_Z (65–90) → lowercase keysym 'a'–'z' (97–122)
        if (vk >= KeyEvent.VK_A && vk <= KeyEvent.VK_Z) {
            return vk + 32;
        }

        // Non-printable special keys
        Integer ks = SPECIAL_KEYS.get(vk);
        if (ks != null) return ks;

        // Other printable keys where VK_ code == Latin-1 value (digits, punctuation, etc.)
        if (vk >= 0x20 && vk <= 0xFE) {
            return vk;
        }

        return 0;
    }

    // -------------------------------------------------------------------------
    // Per-connection VncClient implementation
    // -------------------------------------------------------------------------

    private class ConnectionClient implements VncClient {

        private final VncSocketClientHandle handle;
        private PixelFormat pixelFormat;

        // Renderers — one per encoding type, created fresh in onServerInit
        private RfbRectangleRawRender              rawRender;
        private RfbRectangleCopyRectRender         copyRectRender;
        private RfbRectangleRreRender              rreRender;
        private RfbRectangleCoRreRender            coRreRender;
        private RfbRectangleHextileRender          hextileRender;
        private RfbRectangleZlibRender             zlibRender;
        private RfbRectangleTightBasicRender       tightBasicRender;
        private RfbRectangleTightFillRender        tightFillRender;
        private RfbRectangleTightJpegRender        tightJpegRender;
        private RfbRectangleTightPngFillRender     tightPngFillRender;
        private RfbRectangleTightPngJpegRender     tightPngJpegRender;
        private RfbRectangleTightPngPngRender      tightPngPngRender;
        private RfbRectangleZlibHexRender          zlibHexRender;
        private RfbRectangleZrleRender             zrleRender;
        private RfbRectangleJpegRender             jpegRender;
        private RfbRectangleH264Render             h264Render;
        private RfbRectangleCursorRender           cursorRender;
        private RfbRectangleXCursorRender          xCursorRender;
        private RfbRectangleCursorWithAlphaRender  cursorWithAlphaRender;
        private RfbRectangleDesktopSizeRender      desktopSizeRender;
        private RfbRectangleExtendedDesktopSizeRender extDesktopSizeRender;
        private RfbRectangleLastRectRender         lastRectRender;

        ConnectionClient(VncSocketClientHandle handle) {
            this.handle = handle;
        }

        @Override
        public void onServerInit(ServerInit serverInit) {
            pixelFormat = serverInit.pixelFormat();

            rawRender             = new RfbRectangleRawRender(pixelFormat);
            copyRectRender        = new RfbRectangleCopyRectRender();
            rreRender             = new RfbRectangleRreRender(pixelFormat);
            coRreRender           = new RfbRectangleCoRreRender(pixelFormat);
            hextileRender         = new RfbRectangleHextileRender(pixelFormat);
            zlibRender            = new RfbRectangleZlibRender(pixelFormat);
            tightBasicRender      = new RfbRectangleTightBasicRender(pixelFormat);
            tightFillRender       = new RfbRectangleTightFillRender(pixelFormat);
            tightJpegRender       = new RfbRectangleTightJpegRender();
            tightPngFillRender    = new RfbRectangleTightPngFillRender(pixelFormat);
            tightPngJpegRender    = new RfbRectangleTightPngJpegRender();
            tightPngPngRender     = new RfbRectangleTightPngPngRender();
            zlibHexRender         = new RfbRectangleZlibHexRender(pixelFormat);
            zrleRender            = new RfbRectangleZrleRender(pixelFormat);
            jpegRender            = new RfbRectangleJpegRender();
            h264Render            = new RfbRectangleH264Render();
            cursorRender          = new RfbRectangleCursorRender(pixelFormat);
            xCursorRender         = new RfbRectangleXCursorRender();
            cursorWithAlphaRender = new RfbRectangleCursorWithAlphaRender();
            desktopSizeRender     = new RfbRectangleDesktopSizeRender();
            extDesktopSizeRender  = new RfbRectangleExtendedDesktopSizeRender();
            lastRectRender        = new RfbRectangleLastRectRender();

            int w = serverInit.framebufferWidth();
            int h = serverInit.framebufferHeight();
            synchronized (framebufferLock) {
                framebuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            }

            SwingUtilities.invokeLater(() -> {
                setPreferredSize(new Dimension(w, h));
                revalidate();
                repaint();
            });

            try {
                handle.send(SetEncodings.newBuilder()
                        .encodings(SUPPORTED_ENCODINGS)
                        .build());
                handle.send(FramebufferUpdateRequest.newBuilder()
                        .incremental(false)
                        .x(0).y(0).width(w).height(h)
                        .build());
            } catch (IOException ignored) {}
        }

        @Override
        public void onFramebufferUpdate(FramebufferUpdate msg) {
            int fbWidth, fbHeight;
            synchronized (framebufferLock) {
                if (framebuffer == null) return;
                for (RfbRectangle rect : msg.rectangles()) {
                    applyRectangle(rect);
                }
                fbWidth  = framebuffer.getWidth();
                fbHeight = framebuffer.getHeight();
            }
            repaint();
            try {
                handle.send(FramebufferUpdateRequest.newBuilder()
                        .incremental(true)
                        .x(0).y(0).width(fbWidth).height(fbHeight)
                        .build());
            } catch (IOException ignored) {}
        }

        @Override
        public void onClose() {
            activeHandle = null;
        }

        /**
         * Dispatches a single rectangle to the appropriate renderer.
         * Must be called under {@code framebufferLock}.
         */
        private void applyRectangle(RfbRectangle rect) {
            switch (rect) {
                case RfbRectangleRaw r             -> rawRender.render(r, framebuffer);
                case RfbRectangleCopyRect r        -> copyRectRender.render(r, framebuffer);
                case RfbRectangleRre r             -> rreRender.render(r, framebuffer);
                case RfbRectangleCoRre r           -> coRreRender.render(r, framebuffer);
                case RfbRectangleHextile r         -> hextileRender.render(r, framebuffer);
                case RfbRectangleZlib r            -> zlibRender.render(r, framebuffer);
                case RfbRectangleTightBasic r      -> tightBasicRender.render(r, framebuffer);
                case RfbRectangleTightFill r       -> tightFillRender.render(r, framebuffer);
                case RfbRectangleTightJpeg r       -> tightJpegRender.render(r, framebuffer);
                case RfbRectangleTightPngFill r    -> tightPngFillRender.render(r, framebuffer);
                case RfbRectangleTightPngJpeg r    -> tightPngJpegRender.render(r, framebuffer);
                case RfbRectangleTightPngPng r     -> tightPngPngRender.render(r, framebuffer);
                case RfbRectangleZlibHex r         -> zlibHexRender.render(r, framebuffer);
                case RfbRectangleZrle r            -> zrleRender.render(r, framebuffer);
                case RfbRectangleJpeg r            -> jpegRender.render(r, framebuffer);
                case RfbRectangleH264 r            -> h264Render.render(r, framebuffer);
                case RfbRectangleDesktopSize r     -> resizeFramebuffer(r.width(), r.height());
                case RfbRectangleExtendedDesktopSize r -> {
                    // r.y() == 0 means success; non-zero is an error status
                    if (r.y() == 0) resizeFramebuffer(r.width(), r.height());
                }
                case RfbRectangleCursor r          -> applyCustomCursor(r);
                case RfbRectangleXCursor r         -> applyCustomCursor(r);
                case RfbRectangleCursorWithAlpha r -> applyCustomCursor(r);
                case RfbRectangleLastRect r        -> {} // sentinel: no more rectangles follow
            }
        }

        /**
         * Replaces the framebuffer with a new image at the given dimensions.
         * Must be called under {@code framebufferLock}.
         */
        private void resizeFramebuffer(int w, int h) {
            framebuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            SwingUtilities.invokeLater(() -> {
                setPreferredSize(new Dimension(w, h));
                revalidate();
            });
        }

        private void applyCustomCursor(RfbRectangleCursor r) {
            int w = Math.max(1, r.width());
            int h = Math.max(1, r.height());
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            cursorRender.render(r, img);
            Point hotspot = new Point(Math.min(r.x(), w - 1), Math.min(r.y(), h - 1));
            SwingUtilities.invokeLater(() ->
                    setCursor(Toolkit.getDefaultToolkit().createCustomCursor(img, hotspot, "vnc")));
        }

        private void applyCustomCursor(RfbRectangleXCursor r) {
            int w = Math.max(1, r.width());
            int h = Math.max(1, r.height());
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            xCursorRender.render(r, img);
            Point hotspot = new Point(Math.min(r.x(), w - 1), Math.min(r.y(), h - 1));
            SwingUtilities.invokeLater(() ->
                    setCursor(Toolkit.getDefaultToolkit().createCustomCursor(img, hotspot, "vnc")));
        }

        private void applyCustomCursor(RfbRectangleCursorWithAlpha r) {
            int w = Math.max(1, r.width());
            int h = Math.max(1, r.height());
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            cursorWithAlphaRender.render(r, img);
            Point hotspot = new Point(Math.min(r.x(), w - 1), Math.min(r.y(), h - 1));
            SwingUtilities.invokeLater(() ->
                    setCursor(Toolkit.getDefaultToolkit().createCustomCursor(img, hotspot, "vnc")));
        }
    }
}
