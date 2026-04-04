package io.github.nhwalker.vnc4j.protocol;

import io.github.nhwalker.vnc4j.protocol.internal.ServerInitImpl;

/** Server initialisation message describing the framebuffer and desktop name. */
public non-sealed interface ServerInit extends RfbMessage {
    static Builder newBuilder() {
        return new ServerInitImpl.BuilderImpl();
    }

    int framebufferWidth();
    int framebufferHeight();
    PixelFormat pixelFormat();
    String name();

    interface Builder {
        Builder framebufferWidth(int framebufferWidth);
        Builder framebufferHeight(int framebufferHeight);
        Builder pixelFormat(PixelFormat pixelFormat);
        Builder name(String name);

        ServerInit build();

        default Builder from(ServerInit msg) {
            return framebufferWidth(msg.framebufferWidth()).framebufferHeight(msg.framebufferHeight())
                    .pixelFormat(msg.pixelFormat()).name(msg.name());
        }
    }
}
