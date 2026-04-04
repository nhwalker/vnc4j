package io.github.nhwalker.vnc4j.protocol;

/**
 * TightPNG rectangle (encoding type -260). Sealed base; use one of the sub-types:
 * {@link RfbRectangleTightPngFill}, {@link RfbRectangleTightPngJpeg}, or
 * {@link RfbRectangleTightPngPng}.
 *
 * <p>TightPNG is a variant of Tight that replaces BasicCompression with PNG
 * compression. Fill and JPEG compression types are structurally identical to Tight.
 */
public sealed interface RfbRectangleTightPng extends RfbRectangle
        permits RfbRectangleTightPngFill, RfbRectangleTightPngJpeg, RfbRectangleTightPngPng {

    int ENCODING_TYPE = -260;

    /** Bits 0-3 of the compression-control byte: zlib streams to reset. */
    int streamResets();

    @Override
    default int encodingType() { return ENCODING_TYPE; }
}
