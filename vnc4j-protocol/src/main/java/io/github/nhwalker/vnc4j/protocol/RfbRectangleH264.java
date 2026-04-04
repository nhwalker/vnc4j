package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleH264Impl;

/** H.264 rectangle (encoding type 50). */
public non-sealed interface RfbRectangleH264 extends RfbRectangle {
    int ENCODING_TYPE = 50;
    int FLAG_RESET_CONTEXT      = 0x01;
    int FLAG_RESET_ALL_CONTEXTS = 0x02;

    static Builder newBuilder() {
        return new RfbRectangleH264Impl.BuilderImpl();
    }

    /** Flags: bit 0 = ResetContext, bit 1 = ResetAllContexts. */
    int flags();
    /** H.264 frame bytes (preceded on the wire by a U32 byte count, then U32 flags). */
    byte[] data();

    @Override
    default int encodingType() { return ENCODING_TYPE; }

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder flags(int flags);
        Builder data(byte[] data);

        RfbRectangleH264 build();

        default Builder from(RfbRectangleH264 obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height())
                    .flags(obj.flags()).data(obj.data());
        }
    }
}
