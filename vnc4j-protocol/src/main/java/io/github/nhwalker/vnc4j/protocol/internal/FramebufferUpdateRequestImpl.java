package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.messages.FramebufferUpdateRequest;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public record FramebufferUpdateRequestImpl(
        boolean incremental,
        int x,
        int y,
        int width,
        int height
) implements FramebufferUpdateRequest {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeByte(3); // message-type
        dos.writeByte(incremental ? 1 : 0);
        dos.writeShort(x);
        dos.writeShort(y);
        dos.writeShort(width);
        dos.writeShort(height);
    }

    public static FramebufferUpdateRequest read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        boolean incremental = dis.readUnsignedByte() != 0;
        int x = dis.readUnsignedShort();
        int y = dis.readUnsignedShort();
        int width = dis.readUnsignedShort();
        int height = dis.readUnsignedShort();
        return new FramebufferUpdateRequestImpl(incremental, x, y, width, height);
    }

    public static final class BuilderImpl implements FramebufferUpdateRequest.Builder {
        private boolean incremental;
        private int x;
        private int y;
        private int width;
        private int height;

        @Override public Builder incremental(boolean v) { this.incremental = v; return this; }
        @Override public Builder x(int v) { this.x = v; return this; }
        @Override public Builder y(int v) { this.y = v; return this; }
        @Override public Builder width(int v) { this.width = v; return this; }
        @Override public Builder height(int v) { this.height = v; return this; }

        @Override
        public FramebufferUpdateRequest build() {
            return new FramebufferUpdateRequestImpl(incremental, x, y, width, height);
        }
    }
}
