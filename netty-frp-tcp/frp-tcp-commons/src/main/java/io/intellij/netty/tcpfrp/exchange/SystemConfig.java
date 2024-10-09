package io.intellij.netty.tcpfrp.exchange;

/**
 * SystemConfig
 *
 * @author tech@intellij.io
 */
public class SystemConfig {

    private static final String DATA_FMT_PROPERTY_KEY = "dataPacketUseJson";

    // this is a test
    public static final boolean DATA_PACKET_USE_JSON = Boolean.parseBoolean(System.getProperty(DATA_FMT_PROPERTY_KEY, "false"));

}
