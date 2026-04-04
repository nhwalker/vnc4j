package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleCoRreImpl;
import java.util.List;

/** CoRRE (Compact Rise-and-Run-length Encoding) rectangle (encoding type 4). */
public non-sealed interface RfbRectangleCoRre extends RfbRectangle {
    int ENCODING_TYPE = 4;

    static Builder newBuilder() {
        return new RfbRectangleCoRreImpl.BuilderImpl();
    }

    /** Background pixel: {@code bytesPerPixel} bytes. */
    byte[] background();
    List<CoRreSubrect> subrects();

    @Override
    default int encodingType() { return ENCODING_TYPE; }

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder background(byte[] background);
        Builder subrects(List<CoRreSubrect> subrects);

        RfbRectangleCoRre build();

        default Builder from(RfbRectangleCoRre obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height())
                    .background(obj.background()).subrects(obj.subrects());
        }
    }
}
