package io.github.nhwalker.vnc4j.protocol.messages;

/**
 * Tight rectangle (encoding type 7). Sealed base; use one of the sub-types:
 * {@link RfbRectangleTightFill}, {@link RfbRectangleTightJpeg}, or
 * {@link RfbRectangleTightBasic}.
 *
 * <p>All sub-types carry {@link #streamResets()} (the low nibble of the
 * compression-control byte on the wire, indicating which zlib streams to reset).
 */
public sealed interface RfbRectangleTight extends RfbRectangle
        permits RfbRectangleTightFill, RfbRectangleTightJpeg, RfbRectangleTightBasic {

    int ENCODING_TYPE = 7;

    /** Bits 0-3 of the compression-control byte: zlib streams to reset (bit N = reset stream N). */
    int streamResets();

    @Override
    default int encodingType() { return ENCODING_TYPE; }
}
