package io.intellij.netty.tcpfrp.exchange;

/**
 * SystemConfig
 *
 * @author tech@intellij.io
 */
public class SystemConfig {

    // this is a test
    public static final boolean DATA_PACKET_USE_JSON = Boolean.parseBoolean(System.getProperty("dataPacketUseJson", "false"));

    public static final boolean ENABLE_DISPATCH_LOG = Boolean.parseBoolean(System.getProperty("enableDispatchLog", "false"));

    public static final boolean ENABLE_RANDOM_TYPE = Boolean.parseBoolean(System.getProperty("enableRandomType", "true"));

    public static String details() {
        return String.format("dataPacketUseJson=%s;enableDispatchLog=%s;enableRandomType=%s", DATA_PACKET_USE_JSON, ENABLE_DISPATCH_LOG, ENABLE_RANDOM_TYPE);
    }

}
