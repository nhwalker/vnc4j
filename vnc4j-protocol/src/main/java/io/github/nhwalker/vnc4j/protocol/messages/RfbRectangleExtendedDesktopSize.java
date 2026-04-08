package io.github.nhwalker.vnc4j.protocol.messages;

import io.github.nhwalker.vnc4j.protocol.internal.RfbRectangleExtendedDesktopSizeImpl;
import java.util.List;

/**
 * ExtendedDesktopSize pseudo-encoding rectangle (encoding type -308).
 *
 * <p>The rectangle header fields carry special semantics:
 * <ul>
 *   <li>{@link #x()} – event source: 0 = server-initiated, 1 = client-requested,
 *       2 = other-client-requested.</li>
 *   <li>{@link #y()} – status code: 0 = success, non-zero = error.</li>
 *   <li>{@link #width()} / {@link #height()} – new framebuffer dimensions.</li>
 * </ul>
 */
public non-sealed interface RfbRectangleExtendedDesktopSize extends RfbRectangle {
    int ENCODING_TYPE = -308;

    static Builder newBuilder() {
        return new RfbRectangleExtendedDesktopSizeImpl.BuilderImpl();
    }

    List<Screen> screens();

    @Override
    default int encodingType() { return ENCODING_TYPE; }

    interface Builder {
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder screens(List<Screen> screens);

        RfbRectangleExtendedDesktopSize build();

        default Builder from(RfbRectangleExtendedDesktopSize obj) {
            return x(obj.x()).y(obj.y()).width(obj.width()).height(obj.height())
                    .screens(obj.screens());
        }
    }
}
