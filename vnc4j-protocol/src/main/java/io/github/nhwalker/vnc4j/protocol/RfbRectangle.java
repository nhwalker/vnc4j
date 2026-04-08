package io.github.nhwalker.vnc4j.protocol;

/**
 * Sealed base interface for a single encoded rectangle within a FramebufferUpdate.
 *
 * <p>Use {@link #read(java.io.InputStream, PixelFormat)} to deserialise; the method reads
 * the 10-byte rectangle header and the encoding-specific payload automatically.
 * Each concrete subtype exposes fully typed fields for its payload.
 */
public sealed interface RfbRectangle
        permits RfbRectangleRaw, RfbRectangleCopyRect, RfbRectangleRre,
                RfbRectangleHextile, RfbRectangleZlib, RfbRectangleTight,
                RfbRectangleZrle, RfbRectangleJpeg, RfbRectangleTightPng,
                RfbRectangleDesktopSize, RfbRectangleLastRect, RfbRectangleCursor,
                RfbRectangleExtendedDesktopSize {

    int x();
    int y();
    int width();
    int height();
    int encodingType();

    void write(java.io.OutputStream out) throws java.io.IOException;

    /**
     * Reads a rectangle from the stream: first the 10-byte header (x, y, width, height,
     * encodingType), then the encoding-specific payload. The returned object is an
     * instance of the appropriate sealed subtype.
     *
     * @throws UnsupportedOperationException if the encoding type is JPEG (21) or unknown
     */
    static RfbRectangle read(java.io.InputStream in, PixelFormat pixelFormat)
            throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleDispatch.read(in, pixelFormat);
    }
}
