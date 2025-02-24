package io.intellij.netty.tcp.lb.config;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.List;
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
        try (InputStream inputStream = ConfigParser.class.getClassLoader().getResourceAsStream(configPath)) {
            if (inputStream == null) {
                log.error("config file not found");
                return null;
            }
            List<String> strings = IOUtils.readLines(inputStream, "UTF-8");
            String json = String.join("\r\n", strings);
            // log.info("loadbalancer config json\r\n{}", json);

            JSONObject jsonObject = JSONObject.parseObject(json);

            JSONObject local = jsonObject.getJSONObject("local");
            int port = local.getIntValue("port");

            String lbStrategy = jsonObject.getString("lbStrategy");
            Map<String, Backend> backends = jsonObject.getJSONArray("backends").toJavaList(Backend.class)
                    .stream().collect(Collectors.toMap(Backend::getName, backend -> backend));

            log.warn("TODO validate config");

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
