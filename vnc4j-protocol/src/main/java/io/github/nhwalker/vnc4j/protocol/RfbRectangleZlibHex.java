package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleZlibHexImpl;
import java.util.List;

/**
 * ZlibHex rectangle (encoding type 8). Extends Hextile with optional per-tile
 * zlib compression; each tile may independently be uncompressed, have compressed
 * subrects, or be entirely raw-pixel-compressed.
 */
public non-sealed interface RfbRectangleZlibHex extends RfbRectangle {
    int ENCODING_TYPE = 8;
    int TILE_SIZE = 16;

    static Builder newBuilder() {
        return new RfbRectangleZlibHexImpl.BuilderImpl();
    }

    List<ZlibHexTile> tiles();

    @Override
    default int encodingType() { return ENCODING_TYPE; }

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder tiles(List<ZlibHexTile> tiles);

        RfbRectangleZlibHex build();

        default Builder from(RfbRectangleZlibHex obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height())
                    .tiles(obj.tiles());
        }
    }
}
