package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.FramebufferUpdateImpl;

import java.util.List;

/** Server message delivering one or more encoded rectangles as a framebuffer update. */
public non-sealed interface FramebufferUpdate extends ServerMessage {
    static Builder newBuilder() {
        return new FramebufferUpdateImpl.BuilderImpl();
    }

    List<RfbRectangle> rectangles();

    interface Builder {
        Builder rectangles(List<RfbRectangle> rectangles);

        FramebufferUpdate build();

        default Builder from(FramebufferUpdate msg) {
            return rectangles(msg.rectangles());
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    /**
     * Reads a FramebufferUpdate message from the stream. The {@code pixelFormat} is
     * forwarded to each {@link RfbRectangle#read} call so encoding-specific parsers
     * know the bytes-per-pixel and colour depth.
     */
    static FramebufferUpdate read(java.io.InputStream in, PixelFormat pixelFormat)
            throws java.io.IOException {
        return io.github.nhwalker.vnc4j.protocol.internal.FramebufferUpdateImpl.read(in, pixelFormat);
    }
}
