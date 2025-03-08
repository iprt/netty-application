package io.intellij.netty.tcpfrp;

import io.intellij.netty.tcpfrp.protocol.SslContextUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * SysConfig
 *
 * @author tech@intellij.io
 */
@Data
@Slf4j
public class SysConfig {
    public static final String CONFIG_PATH_PROPERTY = "configPath";
    public static final String DEF_SERVER_CONFIG = "server-config.json";
    public static final String DEF_CLIENT_CONFIG = "client-config.json";

    private static final SysConfig instance = new SysConfig();
    private boolean enableSsl;

    public static SysConfig get() {
        return instance;
    }

    private SysConfig() {
        enableSsl = false;
        SslContextUtils.init(this);
    }

    public void logDetails() {
        log.info("======== SysConfig Details ========");
        log.info("ENABLE_SSL={}", this.enableSsl);
        log.info("===================================");
    }

}
