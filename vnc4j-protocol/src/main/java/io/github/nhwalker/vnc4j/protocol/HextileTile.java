package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.HextileTileImpl;
import java.util.List;

/**
 * A single 16x16 (or smaller at edges) tile within a Hextile encoded rectangle.
 *
 * <p>Subencoding bit flags:
 * <ul>
 *   <li>{@link #SUBENC_RAW} (1) – tile is raw pixel data; all other bits ignored</li>
 *   <li>{@link #SUBENC_BACKGROUND_SPECIFIED} (2) – background pixel follows</li>
 *   <li>{@link #SUBENC_FOREGROUND_SPECIFIED} (4) – foreground pixel follows</li>
 *   <li>{@link #SUBENC_ANY_SUBRECTS} (8) – subrect list follows</li>
 *   <li>{@link #SUBENC_SUBRECTS_COLOURED} (16) – each subrect carries its own pixel</li>
 * </ul>
 */
public interface HextileTile {
    int SUBENC_RAW                 = 1;
    int SUBENC_BACKGROUND_SPECIFIED = 2;
    int SUBENC_FOREGROUND_SPECIFIED = 4;
    int SUBENC_ANY_SUBRECTS        = 8;
    int SUBENC_SUBRECTS_COLOURED   = 16;

    static Builder newBuilder() {
        return new HextileTileImpl.BuilderImpl();
    }

    int subencoding();
    /** Raw pixel data; non-null only when subencoding has {@link #SUBENC_RAW}. */
    byte[] rawPixels();
    /** Background pixel; non-null only when {@link #SUBENC_BACKGROUND_SPECIFIED} is set. */
    byte[] background();
    /** Foreground pixel; non-null only when {@link #SUBENC_FOREGROUND_SPECIFIED} is set. */
    byte[] foreground();
    /** Subrects; non-empty only when {@link #SUBENC_ANY_SUBRECTS} is set. */
    List<HextileSubrect> subrects();

    interface Builder {
        Builder subencoding(int subencoding);
        Builder rawPixels(byte[] rawPixels);
        Builder background(byte[] background);
        Builder foreground(byte[] foreground);
        Builder subrects(List<HextileSubrect> subrects);

        HextileTile build();

        default Builder from(HextileTile obj) {
            return subencoding(obj.subencoding()).rawPixels(obj.rawPixels())
                    .background(obj.background()).foreground(obj.foreground())
                    .subrects(obj.subrects());
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static HextileTile read(java.io.InputStream in, int tileWidth, int tileHeight, int bytesPerPixel)
            throws java.io.IOException {
        return HextileTileImpl.read(in, tileWidth, tileHeight, bytesPerPixel);
    }
}
