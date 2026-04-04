package io.github.nhwalker.vnc4j.protocol;

/** Represents a single screen in a multi-monitor desktop layout. */
public interface Screen {
    long id();
    int x();
    int y();
    int width();
    int height();
    long flags();

    interface Builder {
        Builder id(long id);
        Builder x(int x);
        Builder y(int y);
        Builder width(int width);
        Builder height(int height);
        Builder flags(long flags);

        Screen build();

        default Builder from(Screen msg) {
            return id(msg.id()).x(msg.x()).y(msg.y()).width(msg.width()).height(msg.height()).flags(msg.flags());
        }
    }
}
