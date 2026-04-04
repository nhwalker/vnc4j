package io.github.nhwalker.vnc4j.protocol.internal;

import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.ServerInit;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public record ServerInitImpl(
        int framebufferWidth,
        int framebufferHeight,
        PixelFormat pixelFormat,
        String name
) implements ServerInit {

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeShort(framebufferWidth);
        dos.writeShort(framebufferHeight);
        pixelFormat.write(out);
        byte[] nameBytes = (name != null ? name : "").getBytes(StandardCharsets.UTF_8);
        dos.writeInt(nameBytes.length);
        dos.write(nameBytes);
    }

    public static ServerInit read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        int width = dis.readUnsignedShort();
        int height = dis.readUnsignedShort();
        PixelFormat pf = PixelFormat.read(in);
        int nameLen = dis.readInt();
        byte[] nameBytes = new byte[nameLen];
        dis.readFully(nameBytes);
        String name = new String(nameBytes, StandardCharsets.UTF_8);
        return new ServerInitImpl(width, height, pf, name);
    }

    public static final class BuilderImpl implements ServerInit.Builder {
        private int framebufferWidth;
        private int framebufferHeight;
        private PixelFormat pixelFormat;
        private String name;

        @Override public Builder framebufferWidth(int v) { this.framebufferWidth = v; return this; }
        @Override public Builder framebufferHeight(int v) { this.framebufferHeight = v; return this; }
        @Override public Builder pixelFormat(PixelFormat v) { this.pixelFormat = v; return this; }
        @Override public Builder name(String v) { this.name = v; return this; }

        @Override
        public ServerInit build() {
            return new ServerInitImpl(framebufferWidth, framebufferHeight, pixelFormat, name);
        }
    }
}
