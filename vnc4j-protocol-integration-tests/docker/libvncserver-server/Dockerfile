FROM ubuntu:22.04
RUN apt-get update && DEBIAN_FRONTEND=noninteractive \
    apt-get install -y x11vnc xvfb && \
    rm -rf /var/lib/apt/lists/*
EXPOSE 5900
# Xvfb provides a headless 800x600 framebuffer; x11vnc exports it over RFB with no auth.
# This image uses libvncserver (the library that x11vnc is built against).
CMD ["/bin/bash", "-c", \
     "Xvfb :1 -screen 0 800x600x24 -nolisten tcp & sleep 1 && \
      exec x11vnc -display :1 -rfbport 5900 -nopw -forever -shared -quiet -noipv6"]
