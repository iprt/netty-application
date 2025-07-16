package io.intellij.netty.tcp.lb.config;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ConfigParser
 *
 * @author tech@intellij.io
 * @since 2025-02-20
 */
@Slf4j
public class ConfigParser {
    public static LbConfig loadConfig(String configPath) {
        try (InputStream in = ConfigParser.class.getClassLoader().getResourceAsStream(configPath)) {
            if (in == null) {
                log.error("config file not found");
                return null;
            }
            String json = String.join("\n", IOUtils.readLines(in, "UTF-8"));
            JSONObject obj = JSONObject.parseObject(json);

            int port = obj.getJSONObject("local").getIntValue("port");
            String lbStrategy = obj.getString("lbStrategy");
            Map<String, Backend> backends = obj.getJSONArray("backends").toJavaList(Backend.class)
                    .stream().collect(Collectors.toMap(Backend::getName, b -> b));

            log.warn("TODO : validate config");
            return LbConfig.builder()
                    .port(port)
                    .lbStrategy(LbStrategy.fromString(lbStrategy))
                    .backends(backends)
                    .build();
        } catch (Exception e) {
            log.error("load config error", e);
            return null;
        }
    }

}
