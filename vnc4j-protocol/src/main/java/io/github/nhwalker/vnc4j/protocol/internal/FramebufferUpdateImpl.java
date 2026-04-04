package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.FramebufferUpdate;
import io.github.nhwalker.vnc4j.protocol.RfbRectangle;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public record FramebufferUpdateImpl(List<RfbRectangle> rectangles) implements FramebufferUpdate {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        List<RfbRectangle> rects = rectangles != null ? rectangles : List.of();
        dos.writeByte(0); // message-type
        dos.writeByte(0); // padding
        dos.writeShort(rects.size());
        for (RfbRectangle r : rects) {
            r.write(out);
        }
    }

    public static FramebufferUpdate read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        dis.readUnsignedByte(); // padding
        int count = dis.readUnsignedShort();
        List<RfbRectangle> rects = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            rects.add(RfbRectangle.read(in));
        }
        return new FramebufferUpdateImpl(rects);
    }

    public static final class BuilderImpl implements FramebufferUpdate.Builder {
        private List<RfbRectangle> rectangles;

        @Override
        public Builder rectangles(List<RfbRectangle> v) {
            this.rectangles = v;
            return this;
        }

        @Override
        public FramebufferUpdate build() {
            return new FramebufferUpdateImpl(rectangles);
        }
    }
}
