package io.intellij.netty.tcpfrp.protocol;

import io.intellij.netty.tcpfrp.SysConfig;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Objects;

/**
 * SslContextBuilder
 *
 * @author tech@intellij.io
 * @since 2025-02-17
 */
@Slf4j
public class SslContextUtils {

    private static final String SERVER_CERT = "ssl/server/server.crt";
    private static final String SERVER_KEY = "ssl/server/server.key";
    private static final String CLIENT_CERT = "ssl/client/client.crt";
    private static final String CLIENT_KEY = "ssl/client/client.key";
    private static final String CA_CERT = "ssl/ca.crt";

    /**
     * Builds and returns an SSL context for the server if SSL is enabled in the configuration.
     * The method attempts to load server certificates, private key, and CA certificate to configure the SSL context.
     * If an error occurs during the process, it logs the error and returns null.
     *
     * @return an instance of {@link SslContext} configured for the server, or null if SSL is not enabled
     * or an error occurs during the setup.
     */
    public static SslContext buildServer() {
        if (SysConfig.get().isEnableSsl()) {
            InputStream cert = null;
            InputStream key = null;
            InputStream caCert = null;
            try {
                cert = get(SERVER_CERT);
                key = get(SERVER_KEY);
                caCert = get(CA_CERT);
                return SslContextBuilder
                        .forServer(cert, key).trustManager(caCert)
                        .clientAuth(ClientAuth.REQUIRE)
                        .build();
            } catch (Exception e) {
                log.error("build server error", e);
                return null;
            } finally {
                close(caCert, key, cert);
            }
        }
        return null;
    }

    /**
     * Builds and returns an SSL context for a client connection if SSL is enabled in the system configuration.
     * The method attempts to load the client certificate, key, and CA certificate to construct the SSL context.
     * If an exception occurs during the process, it logs the error and returns null.
     *
     * @return the configured SslContext if SSL is enabled and configured correctly, or null if SSL is disabled
     * or an error occurs during SSL context setup.
     */
    public static SslContext buildClient() {
        if (SysConfig.get().isEnableSsl()) {
            InputStream cert = null;
            InputStream key = null;
            InputStream caCert = null;
            try {
                cert = get(CLIENT_CERT);
                key = get(CLIENT_KEY);
                caCert = get(CA_CERT);
                return SslContextBuilder.forClient()
                        .keyManager(cert, key)
                        .trustManager(caCert)
                        .build();
            } catch (Exception e) {
                log.error("build server error", e);
                return null;
            } finally {
                close(caCert, key, cert);
            }
        }
        return null;
    }

    public static void init(SysConfig sysConfig) {
        try {
            InputStream caCert = get(CA_CERT);
            InputStream serverCrt = get(SERVER_CERT);
            InputStream serverKey = get(SERVER_KEY);
            InputStream clientCrt = get(CLIENT_CERT);
            InputStream clientKey = get(CLIENT_KEY);

            if (Objects.nonNull(caCert)
                    && Objects.nonNull(serverCrt) && Objects.nonNull(serverKey)
                    && Objects.nonNull(clientCrt) && Objects.nonNull(clientKey)
            ) {
                // 待完善验证
                sysConfig.setEnableSsl(true);
            }

            close(caCert, serverKey, serverCrt, clientKey, clientCrt);
        } catch (Exception e) {
            log.error("init error", e);
        }
    }

    private static InputStream get(String path) {
        return SslContextUtils.class.getClassLoader().getResourceAsStream(path);
    }

    static void close(InputStream... inputStreams) {
        for (InputStream inputStream : inputStreams) {
            if (Objects.nonNull(inputStream)) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    log.error("inputStream close occurred error", e);
                }
            }
        }
    }
}
