FROM ubuntu:22.04
# Install libvncserver-dev (provides libvncclient headers and shared library)
# from the LibVNCServer project: https://github.com/LibVNC/libvncserver
RUN apt-get update && DEBIAN_FRONTEND=noninteractive \
    apt-get install -y libvncserver-dev gcc pkg-config && \
    rm -rf /var/lib/apt/lists/*
COPY rfb_connect_test.c /build/rfb_connect_test.c
RUN gcc -o /usr/local/bin/rfb_connect_test /build/rfb_connect_test.c \
    $(pkg-config --cflags --libs libvncclient) && \
    chmod +x /usr/local/bin/rfb_connect_test
ENTRYPOINT ["/usr/local/bin/rfb_connect_test"]
