package io.github.nhwalker.vnc4j.protocol.interop;

import org.testcontainers.images.builder.ImageFromDockerfile;

/**
 * Utility for configuring {@link ImageFromDockerfile} instances so that
 * Docker builds can reach the internet through the environment's HTTP proxy.
 *
 * <p>In proxied CI environments the Docker daemon itself may not have proxy
 * settings, so build-time {@code apt-get} commands fail with DNS errors unless
 * the proxy is forwarded explicitly as build arguments.
 */
final class DockerBuildSupport {

    private DockerBuildSupport() {}

    /**
     * Adds {@code http_proxy}, {@code https_proxy}, {@code HTTP_PROXY}, and
     * {@code HTTPS_PROXY} build arguments from the current process environment,
     * if those variables are set.  apt-get and most other package managers
     * honour at least one of the two case variants.
     *
     * <p>If the environment variables are absent (e.g. on a developer machine
     * with direct internet access) this method is a no-op.
     */
    static ImageFromDockerfile applyProxyBuildArgs(ImageFromDockerfile image) {
        String httpProxy  = System.getenv("http_proxy");
        String httpsProxy = System.getenv("https_proxy");
        if (httpProxy == null) httpProxy  = System.getenv("HTTP_PROXY");
        if (httpsProxy == null) httpsProxy = System.getenv("HTTPS_PROXY");

        if (httpProxy != null) {
            image = image
                    .withBuildArg("http_proxy",  httpProxy)
                    .withBuildArg("HTTP_PROXY",  httpProxy);
        }
        if (httpsProxy != null) {
            image = image
                    .withBuildArg("https_proxy", httpsProxy)
                    .withBuildArg("HTTPS_PROXY", httpsProxy);
        }
        return image;
    }
}
