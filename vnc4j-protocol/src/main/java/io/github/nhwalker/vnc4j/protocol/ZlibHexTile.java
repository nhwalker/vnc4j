package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.ZlibHexTileImpl;
import java.util.List;

/**
 * A single tile within a ZlibHex encoded rectangle. Extends the Hextile subencoding
 * with two additional bits:
 * <ul>
 *   <li>{@link #SUBENC_ZLIB_RAW} (32) – the entire raw tile pixel data is zlib-compressed;
 *       stored in {@link #zlibRawData()}; all other Hextile fields are absent.</li>
 *   <li>{@link #SUBENC_ZLIB} (64) – the subrect portion is zlib-compressed;
 *       stored in {@link #zlibSubrectData()} instead of a subrect list.</li>
 * </ul>
 */
public interface ZlibHexTile {
    int SUBENC_RAW                 = 1;
    int SUBENC_BACKGROUND_SPECIFIED = 2;
    int SUBENC_FOREGROUND_SPECIFIED = 4;
    int SUBENC_ANY_SUBRECTS        = 8;
    int SUBENC_SUBRECTS_COLOURED   = 16;
    int SUBENC_ZLIB_RAW            = 32;
    int SUBENC_ZLIB                = 64;

    static Builder newBuilder() {
        return new ZlibHexTileImpl.BuilderImpl();
    }

    int subencoding();
    /** Non-null when {@link #SUBENC_ZLIB_RAW} is set; replaces all other tile content. */
    byte[] zlibRawData();
    /** Non-null when {@link #SUBENC_BACKGROUND_SPECIFIED} is set and {@link #SUBENC_ZLIB_RAW} is not. */
    byte[] background();
    /** Non-null when {@link #SUBENC_FOREGROUND_SPECIFIED} is set and {@link #SUBENC_ZLIB_RAW} is not. */
    byte[] foreground();
    /** Non-null when {@link #SUBENC_ZLIB} is set; replaces subrect list. */
    byte[] zlibSubrectData();
    /** Non-empty when {@link #SUBENC_ANY_SUBRECTS} is set and neither zlib bit is set. */
    List<HextileSubrect> subrects();

    interface Builder {
        Builder subencoding(int subencoding);
        Builder zlibRawData(byte[] zlibRawData);
        Builder background(byte[] background);
        Builder foreground(byte[] foreground);
        Builder zlibSubrectData(byte[] zlibSubrectData);
        Builder subrects(List<HextileSubrect> subrects);

        ZlibHexTile build();

        default Builder from(ZlibHexTile obj) {
            return subencoding(obj.subencoding()).zlibRawData(obj.zlibRawData())
                    .background(obj.background()).foreground(obj.foreground())
                    .zlibSubrectData(obj.zlibSubrectData()).subrects(obj.subrects());
        }
    }

    void write(java.io.OutputStream out) throws java.io.IOException;

    static ZlibHexTile read(java.io.InputStream in, int tileWidth, int tileHeight, int bytesPerPixel)
            throws java.io.IOException {
        return ZlibHexTileImpl.read(in, tileWidth, tileHeight, bytesPerPixel);
    }
}
