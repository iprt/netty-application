package io.intellij.netty.tcpfrp.exchange;

/**
 * SystemConfig
 *
 * @author tech@intellij.io
 */
public class SystemConfig {

    // this is a test
    public static final boolean DATA_PACKET_USE_JSON = Boolean.parseBoolean(System.getProperty("dataPacketUseJson", "false"));

    public static final boolean DISPATCH_LOG_ENABLE = Boolean.parseBoolean(System.getProperty("dispatchLogEnable", "false"));

}
