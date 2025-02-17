package io.intellij.netty.tcpfrp.exchange;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SysConfig
 *
 * @author tech@intellij.io
 */
@Slf4j
public class SysConfig {

    // this is a test
    public static final boolean DATA_PACKET_USE_JSON = Boolean.parseBoolean(System.getProperty("dataPacketUseJson", "false"));

    public static final boolean ENABLE_DISPATCH_LOG = Boolean.parseBoolean(System.getProperty("enableDispatchLog", "false"));

    public static final boolean ENABLE_RANDOM_TYPE = Boolean.parseBoolean(System.getProperty("enableRandomType", "true"));

    public static final AtomicBoolean ENABLE_SSL = new AtomicBoolean(false);

    public static void logDetails() {
        // return String.format("dataPacketUseJson=%s;enableDispatchLog=%s;enableRandomType=%s;enableSSL=%s",
        //         DATA_PACKET_USE_JSON, ENABLE_DISPATCH_LOG, ENABLE_RANDOM_TYPE, ENABLE_SSL.get());
        log.info("======== SysConfig Details ========");
        log.info("dataPacketUseJson={}", DATA_PACKET_USE_JSON);
        log.info("enableDispatchLog={}", ENABLE_DISPATCH_LOG);
        log.info("enableRandomType={}", ENABLE_RANDOM_TYPE);
        log.info("enableSSL={}", ENABLE_SSL.get());
        log.info("===================================");

    }

    static {
        SslContextUtils.init();
    }

}
