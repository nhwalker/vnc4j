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
        // eventSize and eventType are the first 2 bytes; remaining = eventSize - 2
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
                // Unknown event type: skip the remaining bytes to maintain stream sync
                int remaining = eventSize - 2;
                if (remaining > 0) {
                    byte[] skip = new byte[remaining];
                    dis.readFully(skip);
                }
                return null;
        }
    }
}
