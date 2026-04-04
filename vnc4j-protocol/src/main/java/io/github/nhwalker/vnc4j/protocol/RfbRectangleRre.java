package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleRreImpl;
import java.util.List;

/** RRE (Rise-and-Run-length Encoding) rectangle (encoding type 2). */
public non-sealed interface RfbRectangleRre extends RfbRectangle {
    int ENCODING_TYPE = 2;

    static Builder newBuilder() {
        return new RfbRectangleRreImpl.BuilderImpl();
    }

    /** Background pixel: {@code bytesPerPixel} bytes. */
    byte[] background();
    List<RreSubrect> subrects();

    @Override
    default int encodingType() { return ENCODING_TYPE; }

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder background(byte[] background);
        Builder subrects(List<RreSubrect> subrects);

        RfbRectangleRre build();

        default Builder from(RfbRectangleRre obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height())
                    .background(obj.background()).subrects(obj.subrects());
        }
    }
}
