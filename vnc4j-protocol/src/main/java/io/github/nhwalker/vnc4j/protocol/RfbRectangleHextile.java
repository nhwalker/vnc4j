package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleHextileImpl;
import java.util.List;

/**
 * Hextile rectangle (encoding type 5). The framebuffer area is divided into 16×16 tiles
 * (smaller at right and bottom edges), each with its own subencoding.
 */
public non-sealed interface RfbRectangleHextile extends RfbRectangle {
    int ENCODING_TYPE = 5;
    int TILE_SIZE = 16;

    static Builder newBuilder() {
        return new RfbRectangleHextileImpl.BuilderImpl();
    }

    List<HextileTile> tiles();

    @Override
    default int encodingType() { return ENCODING_TYPE; }

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder tiles(List<HextileTile> tiles);

        RfbRectangleHextile build();

        default Builder from(RfbRectangleHextile obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height())
                    .tiles(obj.tiles());
        }
    }
}
