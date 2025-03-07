package io.intellij.netty.tcpfrp;

import io.intellij.netty.tcpfrp.protocol.SslContextUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SysConfig
 *
 * @author tech@intellij.io
 */
@Slf4j
public class SysConfig {

    public static final AtomicBoolean ENABLE_SSL = new AtomicBoolean(false);

    public static void logDetails() {
        log.info("======== SysConfig Details ========");
        log.info("enableSSL={}", ENABLE_SSL.get());
        log.info("===================================");

    }

    static {
        SslContextUtils.init();
    }

}
