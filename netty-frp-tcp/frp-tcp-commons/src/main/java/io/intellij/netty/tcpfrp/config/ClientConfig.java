package io.intellij.netty.tcpfrp.config;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONPath;
import io.intellij.netty.tcpfrp.SysConfig;
import io.intellij.netty.tcpfrp.protocol.SslContextUtils;
import io.intellij.netty.tcpfrp.protocol.client.ListeningConfig;
import io.netty.handler.ssl.SslContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * ClientConfig
 *
 * @author tech@intellij.io
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
@Slf4j
public class ClientConfig {
    private static final ClientConfig INVALID_CONFIG = ClientConfig.builder().valid(false).build();

    private boolean valid;
    // frp server config
    private String serverHost;
    private int serverPort;
    private String authToken;

    private Map<String, ListeningConfig> listeningConfigMap;

    private boolean enableSSL;
    private SslContext sslContext;

    public static ClientConfig init(InputStream in) {
        try {
            if (in == null) {
                return INVALID_CONFIG;
            }
            String json = IOUtils.readLines(in, "UTF-8").stream().reduce("", (a, b) -> a + b);

            String evalServerHost = (String) JSONPath.eval(json, "$.server.host");
            int evalServerPort = (Integer) JSONPath.eval(json, "$.server.port");
            String evalAuthToken = (String) JSONPath.eval(json, "$.server.auth.token");

            JSONArray array = (JSONArray) JSONPath.eval(json, "$.clients");

            Map<String, ListeningConfig> map = new HashMap<>();
            if (array.isEmpty()) {
                return INVALID_CONFIG;
            } else {
                for (int i = 0; i < array.size(); i++) {
                    String name = (String) JSONPath.eval(json, "$.clients[" + i + "].name");
                    String localIp = (String) JSONPath.eval(json, "$.clients[" + i + "].local_ip");
                    Integer localPort = (Integer) JSONPath.eval(json, "$.clients[" + i + "].local_port");
                    Integer remotePort = (Integer) JSONPath.eval(json, "$.clients[" + i + "].remote_port");

                    if (Objects.isNull(name) || Objects.isNull(localIp) || Objects.isNull(localPort) || Objects.isNull(remotePort)) {
                        return INVALID_CONFIG;
                    }

                    map.put(name, ListeningConfig.create(name, localIp, localPort, remotePort));
                }

                return ClientConfig.builder().valid(true)
                        .serverHost(evalServerHost)
                        .serverPort(evalServerPort)
                        .authToken(evalAuthToken)
                        .listeningConfigMap(map)
                        .enableSSL(SysConfig.get().isEnableSsl())
                        .sslContext(SslContextUtils.buildClient())
                        .build();
            }

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

    public static ClientConfig loadConfig(String path) {
        ClientConfig clientConfig = ClientConfig.init(ClientConfig.class.getClassLoader().getResourceAsStream(path));
        if (clientConfig.isValid()) {
            log.info("client config|{}", clientConfig);
            SysConfig.get().logDetails();
        }
        return clientConfig;
    }

    public void then(Consumer<ClientConfig> consumer) {
        if (this.valid) {
            consumer.accept(this);
        } else {
            log.error("client config is invalid");
        }
    }

}
