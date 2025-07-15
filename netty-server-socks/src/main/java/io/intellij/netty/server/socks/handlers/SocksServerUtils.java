package io.intellij.netty.server.socks.handlers;

import io.intellij.netty.utils.ChannelUtils;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 * SocksServerUtils
 *
 * @author tech@intellij.io
 */
@Slf4j
public final class SocksServerUtils {

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    public static void closeOnFlush(Channel ch) {
        ChannelUtils.closeOnFlush(ch);
    }

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    public static void closeOnFlush(Channel ch, String desc) {
        log.info("close on flush : {}", desc);
        ChannelUtils.closeOnFlush(ch);
    }

    private SocksServerUtils() {
    }
}
