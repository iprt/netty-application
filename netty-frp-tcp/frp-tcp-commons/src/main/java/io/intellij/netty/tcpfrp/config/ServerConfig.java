package io.intellij.netty.tcpfrp.config;

import com.alibaba.fastjson2.JSONPath;
import io.intellij.netty.tcpfrp.exchange.SslContextUtils;
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
    private SslContext sslContext;

    public static ServerConfig init(String where) {

        try (InputStream in = ServerConfig.class.getClassLoader()
                .getResourceAsStream("config.json")) {
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
                    .sslContext(SslContextUtils.buildServer())
                    .build();

        } catch (Exception e) {
            log.error(e.getMessage());
            return INVALID_CONFIG;
        }

    }

}
