package io.github.nhwalker.vnc4j.protocol.interop;

import io.github.nhwalker.vnc4j.protocol.ClientInit;
import io.github.nhwalker.vnc4j.protocol.FramebufferUpdate;
import io.github.nhwalker.vnc4j.protocol.FramebufferUpdateRequest;
import io.github.nhwalker.vnc4j.protocol.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.RfbRectangleRaw;
import io.github.nhwalker.vnc4j.protocol.ServerInit;
import io.github.nhwalker.vnc4j.protocol.SetEncodings;
import io.github.nhwalker.vnc4j.protocol.VncServer;
import io.github.nhwalker.vnc4j.protocol.VncSocketServer;
import io.github.nhwalker.vnc4j.protocol.VncSocketServerHandle;
import org.junit.jupiter.api.Test;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.WaitingConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Interoperability tests for {@link VncSocketServer} against a real
 * libvncclient peer (compiled from {@code rfb_connect_test.c}) running in Docker.
 *
 * <p>libvncserver / libvncclient reference:
 * <a href="https://github.com/LibVNC/libvncserver">
 * https://github.com/LibVNC/libvncserver</a>
 *
 * <p>The container runs a minimal C program that uses the libvncclient API to:
 * <ol>
 *   <li>Connect to our {@link VncSocketServer}.</li>
 *   <li>Complete the full RFB handshake.</li>
 *   <li>Send SetPixelFormat + SetEncodings (via {@code rfbInitClient}).</li>
 *   <li>Send a non-incremental {@link FramebufferUpdateRequest}.</li>
 *   <li>Wait for a {@link FramebufferUpdate} and exit 0 when it arrives.</li>
 * </ol>
 *
 * <p>These tests verify that our Java server-side RFB 3.8 protocol implementation
 * can correctly serve a real libvncclient peer.
 */
class LibvncClientInteropTest {

    // Built once; TestContainers caches Docker images between runs.
    private static final ImageFromDockerfile CLIENT_IMAGE = DockerBuildSupport.applyProxyBuildArgs(
            new ImageFromDockerfile()
                    .withFileFromClasspath("Dockerfile",
                            "docker/libvncclient-client/Dockerfile")
                    .withFileFromClasspath("rfb_connect_test.c",
                            "docker/libvncclient-client/rfb_connect_test.c"));

    // 32bpp BGR0 — the de-facto default advertised by most libvncserver-based servers.
    private static final PixelFormat BGR0 = PixelFormat.newBuilder()
            .bitsPerPixel(32).depth(24).bigEndian(false).trueColour(true)
            .redMax(255).greenMax(255).blueMax(255)
            .redShift(16).greenShift(8).blueShift(0)
            .build();

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    /**
     * A libvncclient peer must be able to complete the full RFB handshake with
     * our server and receive a valid {@link FramebufferUpdate} in response to
     * its first {@link FramebufferUpdateRequest}.
     *
     * <p>Assertions:
     * <ul>
     *   <li>Our server's {@link VncServer#onClientInit} is called.</li>
     *   <li>Our server's {@link VncServer#onSetEncodings} is called (libvncclient
     *       always sends SetEncodings during {@code rfbInitClient}).</li>
     *   <li>Our server's {@link VncServer#onFramebufferUpdateRequest} is called.</li>
     *   <li>The libvncclient container logs that it received a FramebufferUpdate
     *       and exits 0.</li>
     * </ul>
     */
    @Test
    void testLibvnclientHandshakeAndFramebufferUpdateExchange() throws Exception {
        CountDownLatch clientInitLatch   = new CountDownLatch(1);
        CountDownLatch encodingsLatch    = new CountDownLatch(1);
        CountDownLatch fbRequestLatch    = new CountDownLatch(1);
        AtomicReference<ClientInit> clientInitRef = new AtomicReference<>();
        AtomicReference<SetEncodings> encodingsRef = new AtomicReference<>();
        AtomicReference<FramebufferUpdateRequest> fbRequestRef = new AtomicReference<>();

        try (VncSocketServer server = new VncSocketServer(0, handle -> makeVncServer(
                handle,
                clientInitLatch, clientInitRef,
                encodingsLatch, encodingsRef,
                fbRequestLatch, fbRequestRef))) {

            server.start();
            int port = server.getLocalPort();

            // Make the host port reachable from inside the container via
            // the special hostname "host.testcontainers.internal".
            Testcontainers.exposeHostPorts(port);

            WaitingConsumer logConsumer = new WaitingConsumer();
            try (GenericContainer<?> container = new GenericContainer<>(CLIENT_IMAGE)
                    .withEnv("VNC_HOST", "host.testcontainers.internal")
                    .withEnv("VNC_PORT", String.valueOf(port))
                    .waitingFor(Wait.forLogMessage(".*", 1))
                    .withLogConsumer(logConsumer)) {

                container.start();

                // --- Java-side assertions ---

                assertTrue(clientInitLatch.await(15, TimeUnit.SECONDS),
                        "VncServer.onClientInit was not called within 15 s");

                assertTrue(encodingsLatch.await(15, TimeUnit.SECONDS),
                        "VncServer.onSetEncodings was not called within 15 s; "
                        + "libvncclient always sends SetEncodings after rfbInitClient");

                assertTrue(fbRequestLatch.await(15, TimeUnit.SECONDS),
                        "VncServer.onFramebufferUpdateRequest was not called within 15 s");

                SetEncodings enc = encodingsRef.get();
                assertNotNull(enc);
                assertFalse(enc.encodings().isEmpty(),
                        "SetEncodings must list at least one encoding");

                FramebufferUpdateRequest req = fbRequestRef.get();
                assertNotNull(req);
                assertEquals(0, req.x(), "FBUpdateRequest x should be 0");
                assertEquals(0, req.y(), "FBUpdateRequest y should be 0");
                assertTrue(req.width() > 0, "FBUpdateRequest width must be > 0");
                assertTrue(req.height() > 0, "FBUpdateRequest height must be > 0");

                // --- Container-side assertion ---
                // The C program logs "FramebufferUpdate received" and exits 0.
                logConsumer.waitUntil(
                        frame -> frame.getUtf8String().contains("FramebufferUpdate received"),
                        15, TimeUnit.SECONDS);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Constructs the minimal ServerInit that vnc4j advertises to libvncclient. */
    private static ServerInit buildServerInit() {
        return ServerInit.newBuilder()
                .framebufferWidth(800)
                .framebufferHeight(600)
                .pixelFormat(BGR0)
                .name("vnc4j-interop-test")
                .build();
    }

    /**
     * Sends a minimal 1×1 Raw black rectangle in response to a
     * {@link FramebufferUpdateRequest}.  This is enough to satisfy
     * libvncclient's {@code GotFrameBufferUpdate} callback.
     */
    private static void sendMinimalFramebufferUpdate(VncSocketServerHandle handle)
            throws Exception {
        byte[] blackPixel = new byte[4]; // 4 zero bytes = black pixel in BGR0
        RfbRectangleRaw rect = RfbRectangleRaw.newBuilder()
                .x(0).y(0).width(1).height(1)
                .pixels(blackPixel)
                .build();
        handle.send(FramebufferUpdate.newBuilder()
                .rectangles(List.of(rect))
                .build());
    }

    /** Builds the {@link VncServer} stub used during the test. */
    private static VncServer makeVncServer(
            VncSocketServerHandle handle,
            CountDownLatch clientInitLatch,
            AtomicReference<ClientInit> clientInitRef,
            CountDownLatch encodingsLatch,
            AtomicReference<SetEncodings> encodingsRef,
            CountDownLatch fbRequestLatch,
            AtomicReference<FramebufferUpdateRequest> fbRequestRef) {

        return new VncServer() {
            @Override
            public ServerInit onClientInit(ClientInit ci) {
                clientInitRef.set(ci);
                clientInitLatch.countDown();
                return buildServerInit();
            }

            @Override
            public void onSetEncodings(SetEncodings m) {
                encodingsRef.set(m);
                encodingsLatch.countDown();
            }

            @Override
            public void onFramebufferUpdateRequest(FramebufferUpdateRequest req) {
                fbRequestRef.set(req);
                fbRequestLatch.countDown();
                try {
                    sendMinimalFramebufferUpdate(handle);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to send FramebufferUpdate", e);
                }
            }
        };
    }
}
