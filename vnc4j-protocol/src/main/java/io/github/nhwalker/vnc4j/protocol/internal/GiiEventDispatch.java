package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.GiiEvent;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/** Dispatches GII event reading based on event-type byte. */
public final class GiiEventDispatch {
    private GiiEventDispatch() {}

    public static GiiEvent readEvent(InputStream in, boolean bigEndian) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        int eventSize = dis.readUnsignedByte();
        int eventType = dis.readUnsignedByte();
        // Remaining bytes after size and type have already been consumed above
        // Each readWithType method reads the remaining bytes for its event type
        switch (eventType) {
            case 5: case 6: case 7:
                return GiiKeyEventImpl.readWithType(in, bigEndian, eventType);
            case 8: case 9:
                return GiiPointerMoveEventImpl.readWithType(in, bigEndian, eventType);
            case 10: case 11:
                return GiiPointerButtonEventImpl.readWithType(in, bigEndian, eventType);
            case 12: case 13:
                return GiiValuatorEventImpl.readWithType(in, bigEndian, eventType);
            default:
                // Unknown event type: skip remaining bytes (eventSize - 2 already read)
                dis.skipBytes(eventSize - 2);
                throw new IOException("Unknown GII event type: " + eventType);
        }
    }
}
