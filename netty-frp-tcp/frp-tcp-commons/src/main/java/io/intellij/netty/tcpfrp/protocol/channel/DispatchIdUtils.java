package io.intellij.netty.tcpfrp.protocol.channel;

import io.netty.channel.Channel;

/**
 * DispatchIdUtils
 *
 * @author tech@intellij.io
 * @since 2025-03-07
 */
public class DispatchIdUtils {
    public static final int ID_LENGTH = 60;

    private DispatchIdUtils() {
        // Prevent instantiation
    }

    public static String getDispatchId(Channel channel) {
        if (channel == null) {
            throw new IllegalArgumentException("Channel cannot be null");
        }
        return channel.id().asLongText();
    }

}
