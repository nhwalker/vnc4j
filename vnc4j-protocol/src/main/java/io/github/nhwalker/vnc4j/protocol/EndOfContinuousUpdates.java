package io.github.nhwalker.vnc4j.protocol;

/** Server message signalling the end of continuous update mode. */
public non-sealed interface EndOfContinuousUpdates extends ServerMessage {

    interface Builder {
        EndOfContinuousUpdates build();

        default Builder from(EndOfContinuousUpdates msg) {
            return this;
        }
    }
}
