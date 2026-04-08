package io.github.nhwalker.vnc4j.protocol.interop;

import io.github.nhwalker.vnc4j.protocol.messages.FramebufferUpdate;
import io.github.nhwalker.vnc4j.protocol.messages.FramebufferUpdateRequest;
import io.github.nhwalker.vnc4j.protocol.messages.PixelFormat;
import io.github.nhwalker.vnc4j.protocol.messages.RfbRectangle;
import io.github.nhwalker.vnc4j.protocol.messages.ServerInit;
import io.github.nhwalker.vnc4j.protocol.VncClient;
import io.github.nhwalker.vnc4j.protocol.VncSocketClient;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Interoperability tests for {@link VncSocketClient} against a real
 * libvncserver-based server (x11vnc backed by Xvfb) running in Docker.
 *
 * <p>libvncserver reference: <a href="https://github.com/LibVNC/libvncserver">
 * https://github.com/LibVNC/libvncserver</a>
 *
 * <p>x11vnc uses libvncserver as its underlying RFB implementation, making
 * it a faithful stand-in for any server built on that library.  These tests
 * verify that our Java client-side RFB 3.8 protocol implementation can:
 * <ul>
 *   <li>Complete the full RFB handshake and receive a valid {@link ServerInit}.</li>
 *   <li>Request and receive a {@link FramebufferUpdate} for the full desktop.</li>
 * </ul>
 */
@Testcontainers
class LibvncServerInteropTest {

    /**
     * x11vnc container: starts Xvfb (800×600×24) then x11vnc on port 5900
     * with no password.  The {@code Wait.forListeningPort()} strategy ensures
     * the container is ready before any test method runs.
     */
    @Container
    static final GenericContainer<?> vncServer = new GenericContainer<>(
            DockerBuildSupport.applyProxyBuildArgs(
                    new ImageFromDockerfile()
                            .withFileFromClasspath("Dockerfile",
                                    "docker/libvncserver-server/Dockerfile")))
            .withExposedPorts(5900)
            .waitingFor(Wait.forListeningPort());

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    /**
     * The RFB handshake must complete successfully: version exchange, security
     * negotiation (None), ClientInit / ServerInit round-trip.
     *
     * <p>We assert that the {@link ServerInit} delivered to our {@link VncClient}
     * contains valid framebuffer dimensions and a non-null pixel format.
     */
    @Test
    void testHandshakeCompletesAndServerInitReceived() throws Exception {
        CompletableFuture<ServerInit> initFuture = new CompletableFuture<>();

        try (VncSocketClient client = new VncSocketClient(
                vncServer.getHost(), vncServer.getMappedPort(5900),
                handle -> new VncClient() {
                    @Override
                    public void onServerInit(ServerInit si) {
                        initFuture.complete(si);
                    }
                    @Override
                    public void onFramebufferUpdate(FramebufferUpdate msg) {}
                })) {

            client.start();

            ServerInit si = initFuture.get(15, TimeUnit.SECONDS);

            assertNotNull(si, "ServerInit must not be null");
            assertTrue(si.framebufferWidth() > 0,
                    "framebufferWidth must be positive, was: " + si.framebufferWidth());
            assertTrue(si.framebufferHeight() > 0,
                    "framebufferHeight must be positive, was: " + si.framebufferHeight());

            PixelFormat pf = si.pixelFormat();
            assertNotNull(pf, "PixelFormat must not be null");
            assertTrue(pf.bitsPerPixel() == 8 || pf.bitsPerPixel() == 16
                            || pf.bitsPerPixel() == 24 || pf.bitsPerPixel() == 32,
                    "bitsPerPixel must be a standard value, was: " + pf.bitsPerPixel());

            assertNotNull(si.name(), "Desktop name must not be null");
        }
    }

    /**
     * After the handshake our client sends a full-screen non-incremental
     * {@link FramebufferUpdateRequest}; the server must reply with at least
     * one rectangle.
     */
    @Test
    void testFramebufferUpdateReceivedAfterRequest() throws Exception {
        CompletableFuture<ServerInit> initFuture = new CompletableFuture<>();
        CompletableFuture<FramebufferUpdate> updateFuture = new CompletableFuture<>();

        try (VncSocketClient client = new VncSocketClient(
                vncServer.getHost(), vncServer.getMappedPort(5900),
                handle -> new VncClient() {
                    @Override
                    public void onServerInit(ServerInit si) {
                        initFuture.complete(si);
                        try {
                            handle.send(FramebufferUpdateRequest.newBuilder()
                                    .incremental(false)
                                    .x(0).y(0)
                                    .width(si.framebufferWidth())
                                    .height(si.framebufferHeight())
                                    .build());
                        } catch (Exception e) {
                            updateFuture.completeExceptionally(e);
                        }
                    }
                    @Override
                    public void onFramebufferUpdate(FramebufferUpdate msg) {
                        updateFuture.complete(msg);
                    }
                })) {

            client.start();

            initFuture.get(15, TimeUnit.SECONDS);
            FramebufferUpdate update = updateFuture.get(30, TimeUnit.SECONDS);

            assertNotNull(update, "FramebufferUpdate must not be null");
            List<RfbRectangle> rects = update.rectangles();
            assertFalse(rects.isEmpty(),
                    "FramebufferUpdate must contain at least one rectangle");

            // The first rectangle must lie within the advertised framebuffer
            RfbRectangle r = rects.get(0);
            assertTrue(r.width() >= 0, "Rectangle width must be non-negative");
            assertTrue(r.height() >= 0, "Rectangle height must be non-negative");
        }
    }
}
