package io.github.nhwalker.vnc4j.protocol;

/** Client message instructing the server to use a specific pixel format. */
public interface SetPixelFormat extends ClientMessage {
    PixelFormat pixelFormat();
}
