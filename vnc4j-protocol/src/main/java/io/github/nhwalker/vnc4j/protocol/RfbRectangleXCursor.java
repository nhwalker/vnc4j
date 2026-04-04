package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleXCursorImpl;

/**
 * XCursor pseudo-encoding rectangle (encoding type -240). Delivers an X-style two-colour
 * cursor. When {@link #width()} or {@link #height()} is 0 the colour and bitmap fields
 * are absent (the cursor is made invisible).
 */
public non-sealed interface RfbRectangleXCursor extends RfbRectangle {
    int ENCODING_TYPE = -240;

    static Builder newBuilder() {
        return new RfbRectangleXCursorImpl.BuilderImpl();
    }

    /** Primary (foreground) colour – red component (0–255). 0 when width or height is 0. */
    int primaryR();
    /** Primary colour – green component. */
    int primaryG();
    /** Primary colour – blue component. */
    int primaryB();
    /** Secondary (background) colour – red component. 0 when width or height is 0. */
    int secondaryR();
    /** Secondary colour – green component. */
    int secondaryG();
    /** Secondary colour – blue component. */
    int secondaryB();
    /**
     * Cursor bitmap (foreground mask): {@code ⌈width/8⌉ × height} bytes.
     * Null when width or height is 0.
     */
    byte[] bitmap();
    /**
     * Cursor bitmask (valid pixel mask): {@code ⌈width/8⌉ × height} bytes.
     * Null when width or height is 0.
     */
    byte[] bitmask();

    @Override
    default int encodingType() { return ENCODING_TYPE; }

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder primaryR(int primaryR);
        Builder primaryG(int primaryG);
        Builder primaryB(int primaryB);
        Builder secondaryR(int secondaryR);
        Builder secondaryG(int secondaryG);
        Builder secondaryB(int secondaryB);
        Builder bitmap(byte[] bitmap);
        Builder bitmask(byte[] bitmask);

        RfbRectangleXCursor build();

        default Builder from(RfbRectangleXCursor obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height())
                    .primaryR(obj.primaryR()).primaryG(obj.primaryG()).primaryB(obj.primaryB())
                    .secondaryR(obj.secondaryR()).secondaryG(obj.secondaryG()).secondaryB(obj.secondaryB())
                    .bitmap(obj.bitmap()).bitmask(obj.bitmask());
        }
    }
}
