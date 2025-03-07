package io.intellij.netty.tcpfrp.protocol.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * FrpChannel
 *
 * @author tech@intellij.io
 * @since 2025-03-06
 */
@RequiredArgsConstructor
@Slf4j
public class FrpChannel {

    private final Channel channel;

    public static FrpChannel build(Channel channel) {
        return new FrpChannel(channel);
    }

    public ChannelFuture writeAndFlush(Object msg, ChannelFutureListener... listeners) {
        if (channel.isActive()) {
            return channel.writeAndFlush(msg).addListeners(listeners);
        }
        log.error("Channel is not active, cannot write and flush message");
        return null;
    }

    public void read() {
        if (channel.isActive()) {
            channel.read();
        } else {
            log.warn("Channel is not active, cannot read");
        }
    }

}
