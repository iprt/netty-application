package io.intellij.netty.tcpfrp.config;

import com.alibaba.fastjson2.JSONPath;
import io.intellij.netty.tcpfrp.SysConfig;
import io.intellij.netty.tcpfrp.protocol.SslContextUtils;
import io.netty.handler.ssl.SslContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * ServerConfig
 *
 * @author tech@intellij.io
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
@Slf4j
public class ServerConfig {
    private static final ServerConfig INVALID_CONFIG = ServerConfig.builder().valid(false).build();

    private boolean valid;
    private String host;
    private int port;
    private String authToken;

    private boolean enableSSL;
    private SslContext sslContext;

    public static ServerConfig init(InputStream in) {
        try {
            if (in == null) {
                return INVALID_CONFIG;
            }

            String json = IOUtils.readLines(in, "UTF-8").stream().reduce("", (a, b) -> a + b);

            String host = (String) JSONPath.eval(json, "$.server.host");
            Integer port = (Integer) JSONPath.eval(json, "$.server.port");
            String authToken = (String) JSONPath.eval(json, "$.server.auth.token");

            if (Objects.isNull(host) || Objects.isNull(port) || Objects.isNull(authToken)) {
                return INVALID_CONFIG;
            }
            return ServerConfig.builder().valid(true)
                    .host(host).port(port)
                    .authToken(authToken)
                    .enableSSL(SysConfig.get().isEnableSsl())
                    .sslContext(SslContextUtils.buildServer())
                    .build();

        } catch (Exception e) {
            log.error(e.getMessage());
            return INVALID_CONFIG;
        } finally {
            if (Objects.nonNull(in)) {
                try {
                    in.close();
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }

    }

    public static ServerConfig loadConfig(String path) {
        ServerConfig serverConfig = ServerConfig.init(ServerConfig.class.getClassLoader().getResourceAsStream(path));
        if (serverConfig.isValid()) {
            log.info("server config|{}", serverConfig);
            SysConfig.get().logDetails();
        }
        return serverConfig;
    }


    public void then(Consumer<ServerConfig> consumer) {
        if (this.valid) {
            consumer.accept(this);
        } else {
            log.error("server config is invalid");
        }
    }
}
