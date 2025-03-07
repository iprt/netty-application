package io.intellij.netty.tcpfrp.exchange;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.RequiredArgsConstructor;

/**
 * FrpsChannel
 *
 * @author tech@intellij.io
 * @since 2025-03-06
 */
@RequiredArgsConstructor
public class FrpChannel {

    private final Channel channel;

    public static FrpChannel build(Channel channel) {
        return new FrpChannel(channel);
    }

    public ChannelFuture writeAndFlush(Object msg) {
        if (channel.isActive()) {
            return channel.writeAndFlush(msg);
        } else {
            throw new IllegalStateException("Channel is not active");
        }
    }

    public void read() {
        if (channel.isActive()) {
            channel.read();
        } else {
            throw new IllegalStateException("Channel is not active");
        }
    }
}
