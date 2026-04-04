package io.github.nhwalker.vnc4j.protocol;

import java.util.List;

/** Server message delivering one or more encoded rectangles as a framebuffer update. */
public non-sealed interface FramebufferUpdate extends ServerMessage {
    List<RfbRectangle> rectangles();

    interface Builder {
        Builder rectangles(List<RfbRectangle> rectangles);

        FramebufferUpdate build();

        default Builder from(FramebufferUpdate msg) {
            return rectangles(msg.rectangles());
        }
    }
}
