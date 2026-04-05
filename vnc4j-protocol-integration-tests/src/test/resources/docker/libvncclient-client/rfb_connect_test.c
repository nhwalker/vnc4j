/*
 * rfb_connect_test.c
 *
 * Minimal VNC client built against libvncclient (from the LibVNCServer project:
 * https://github.com/LibVNC/libvncserver).
 *
 * Behaviour:
 *   1. Connects to the VNC server at $VNC_HOST:$VNC_PORT (defaults: localhost:5900).
 *   2. Completes the full RFB 3.x handshake (None security).
 *   3. Sends SetPixelFormat + SetEncodings (via rfbInitClient).
 *   4. Sends a full-screen FramebufferUpdateRequest.
 *   5. Waits up to 30 seconds for a FramebufferUpdate rectangle.
 *   6. Exits 0 on success, 1 on any failure or timeout.
 *
 * This binary is used by LibvncClientInteropTest to verify that vnc4j's
 * VncSocketServer correctly handles a real libvncclient peer.
 */
#include <rfb/rfbclient.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

static void got_framebuffer_update(rfbClient *cl, int x, int y, int w, int h) {
    (void)x; (void)y; (void)w; (void)h;
    rfbClientLog("FramebufferUpdate received — server is compatible\n");
    rfbClientCleanup(cl);
    exit(0);
}

int main(void) {
    const char *host = getenv("VNC_HOST");
    const char *port_str = getenv("VNC_PORT");
    if (!host || host[0] == '\0') host = "localhost";
    int port = (port_str && port_str[0] != '\0') ? atoi(port_str) : 5900;

    rfbClient *cl = rfbGetClient(8, 3, 4); /* 8 bps, 3 samples, 4 bytes/pixel */
    if (!cl) {
        fprintf(stderr, "rfbGetClient failed\n");
        return 1;
    }

    cl->GotFrameBufferUpdate = got_framebuffer_update;
    cl->serverHost = strdup(host);
    cl->serverPort = port;
    cl->appData.useRemoteCursor = FALSE;

    /* rfbInitClient connects, performs the RFB handshake, and sends
     * SetPixelFormat + SetEncodings.  Passing argc=0/argv=NULL skips
     * command-line argument processing and uses serverHost/serverPort directly. */
    int argc = 0;
    if (!rfbInitClient(cl, &argc, NULL)) {
        fprintf(stderr, "rfbInitClient failed: could not connect to %s:%d\n", host, port);
        return 1;
    }

    /* Request a full-screen non-incremental update */
    if (!SendFramebufferUpdateRequest(cl, 0, 0, cl->width, cl->height, FALSE)) {
        fprintf(stderr, "SendFramebufferUpdateRequest failed\n");
        rfbClientCleanup(cl);
        return 1;
    }

    /* Poll for up to 30 seconds (300 * 100 ms) */
    for (int i = 0; i < 300; i++) {
        int ready = WaitForMessage(cl, 100000); /* 100 ms */
        if (ready < 0) {
            fprintf(stderr, "WaitForMessage error\n");
            break;
        }
        if (ready > 0) {
            if (!HandleRFBServerMessage(cl)) {
                fprintf(stderr, "HandleRFBServerMessage failed\n");
                break;
            }
        }
    }

    rfbClientCleanup(cl);
    fprintf(stderr, "Timeout: no FramebufferUpdate received from %s:%d\n", host, port);
    return 1;
}
