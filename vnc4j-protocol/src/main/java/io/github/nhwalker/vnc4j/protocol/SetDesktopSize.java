package io.github.nhwalker.vnc4j.protocol;

import java.util.List;

/** Client message requesting a desktop resize with a new set of screen layouts. */
public interface SetDesktopSize extends ClientMessage {
    int width();
    int height();
    List<Screen> screens();
}
