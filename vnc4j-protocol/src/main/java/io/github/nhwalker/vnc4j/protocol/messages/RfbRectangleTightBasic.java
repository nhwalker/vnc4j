package io.github.nhwalker.vnc4j.protocol.messages;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleTightBasicImpl;

/**
 * Tight BasicCompression rectangle (encoding type 7, compression type 0x0-0x7).
 * Pixel data is zlib-compressed, optionally transformed by a filter.
 */
public non-sealed interface RfbRectangleTightBasic extends RfbRectangleTight {

    int FILTER_COPY     = 0;
    int FILTER_PALETTE  = 1;
    int FILTER_GRADIENT = 2;

    static Builder newBuilder() {
        return new RfbRectangleTightBasicImpl.BuilderImpl();
    }

    /** Zlib stream index (0-3) used for data compression. */
    int streamNumber();
    /** Filter type: {@link #FILTER_COPY}, {@link #FILTER_PALETTE}, or {@link #FILTER_GRADIENT}. */
    int filterType();
    /**
     * Number of palette entries; non-zero only when {@link #filterType()} is
     * {@link #FILTER_PALETTE}. Each entry is one TPIXEL.
     */
    int paletteSize();
    /**
     * Palette entries as raw bytes: {@link #paletteSize()} × TPIXEL bytes.
     * Null when {@link #filterType()} is not {@link #FILTER_PALETTE}.
     */
    byte[] palette();
    /** Zlib-compressed pixel data (preceded on the wire by a compact-length prefix). */
    byte[] compressedData();

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder streamResets(int streamResets);
        Builder streamNumber(int streamNumber);
        Builder filterType(int filterType);
        Builder paletteSize(int paletteSize);
        Builder palette(byte[] palette);
        Builder compressedData(byte[] compressedData);

        RfbRectangleTightBasic build();

        default Builder from(RfbRectangleTightBasic obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height())
                    .streamResets(obj.streamResets()).streamNumber(obj.streamNumber())
                    .filterType(obj.filterType()).paletteSize(obj.paletteSize())
                    .palette(obj.palette()).compressedData(obj.compressedData());
        }
    }
}
