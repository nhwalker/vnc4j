package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.RreSubrectImpl;

/** A subrectangle within an RRE encoded rectangle. Coordinates are U16. */
public interface RreSubrect {
    static Builder newBuilder() {
        return new RreSubrectImpl.BuilderImpl();
    }

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

        RreSubrect build();

        default Builder from(RreSubrect obj) {
            return pixel(obj.pixel()).x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height());
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static RreSubrect read(java.io.InputStream in, int bytesPerPixel) throws java.io.IOException {
        return RreSubrectImpl.read(in, bytesPerPixel);
    }
}
