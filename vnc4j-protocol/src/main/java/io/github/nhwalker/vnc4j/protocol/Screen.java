package io.github.nhwalker.vnc4j.protocol;

/** Represents a single screen in a multi-monitor desktop layout. */
public interface Screen {
    long id();
    int x();
    int y();
    int width();
    int height();
    long flags();
}
