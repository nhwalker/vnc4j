package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.CoRreSubrectImpl;

/** A subrectangle within a CoRRE encoded rectangle. Coordinates are U8 (0-255). */
public interface CoRreSubrect {
    static Builder newBuilder() {
        return new CoRreSubrectImpl.BuilderImpl();
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

        CoRreSubrect build();

        default Builder from(CoRreSubrect obj) {
            return pixel(obj.pixel()).x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height());
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static CoRreSubrect read(java.io.InputStream in, int bytesPerPixel) throws java.io.IOException {
        return CoRreSubrectImpl.read(in, bytesPerPixel);
    }
}
