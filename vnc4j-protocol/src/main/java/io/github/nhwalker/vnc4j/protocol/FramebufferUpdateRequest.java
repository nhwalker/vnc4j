package io.github.nhwalker.vnc4j.protocol;

/** Client request for a framebuffer update for the specified region. */
public non-sealed interface FramebufferUpdateRequest extends ClientMessage {
    boolean incremental();
    int x();
    int y();
    int width();
    int height();

    interface Builder {
        Builder incremental(boolean incremental);
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);

        FramebufferUpdateRequest build();

        default Builder from(FramebufferUpdateRequest msg) {
            return incremental(msg.incremental()).x(msg.x()).y(msg.y()).width(msg.width()).height(msg.height());
        }
    }
}
