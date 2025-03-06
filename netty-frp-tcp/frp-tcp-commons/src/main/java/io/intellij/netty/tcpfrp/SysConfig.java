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

    public static final boolean ENABLE_DISPATCH_LOG = Boolean.parseBoolean(System.getProperty("enableDispatchLog", "false"));

    public static final boolean ENABLE_RANDOM_TYPE = Boolean.parseBoolean(System.getProperty("enableRandomType", "false"));

    public static final AtomicBoolean ENABLE_SSL = new AtomicBoolean(false);

    public static void logDetails() {
        // return String.format("dataPacketUseJson=%s;enableDispatchLog=%s;enableRandomType=%s;enableSSL=%s",
        //         DATA_PACKET_USE_JSON, ENABLE_DISPATCH_LOG, ENABLE_RANDOM_TYPE, ENABLE_SSL.get());
        log.info("======== SysConfig Details ========");
        log.info("enableDispatchLog={}", ENABLE_DISPATCH_LOG);
        log.info("enableRandomType={}", ENABLE_RANDOM_TYPE);
        log.info("enableSSL={}", ENABLE_SSL.get());
        log.info("===================================");

    }

    static {
        SslContextUtils.init();
    }

}
