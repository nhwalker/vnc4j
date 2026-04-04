package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.HextileSubrectImpl;

/**
 * A subrectangle within a Hextile tile. Coordinates and dimensions are packed
 * as 4-bit values: x, y in [0,15] relative to the tile origin; width and height
 * in [1,16].
 */
public interface HextileSubrect {
    static Builder newBuilder() {
        return new HextileSubrectImpl.BuilderImpl();
    }

    /**
     * Per-subrect foreground pixel; non-null only when the enclosing tile has
     * the SubrectsColoured (16) subencoding bit set.
     */
    byte[] pixel();
    int x();
    int y();
    int width();
    int height();

    interface Builder {
        Builder pixel(byte[] pixel);
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);

        HextileSubrect build();

        default Builder from(HextileSubrect obj) {
            return pixel(obj.pixel()).x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height());
        }
    }

    /**
     * Writes this subrect. If {@code subrectsColoured} is true the pixel bytes
     * are written first, then the packed xy byte and packed wh byte.
     */
    void write(java.io.OutputStream out, boolean subrectsColoured) throws java.io.IOException;

    static HextileSubrect read(java.io.InputStream in, boolean subrectsColoured, int bytesPerPixel)
            throws java.io.IOException {
        return HextileSubrectImpl.read(in, subrectsColoured, bytesPerPixel);
    }
}
