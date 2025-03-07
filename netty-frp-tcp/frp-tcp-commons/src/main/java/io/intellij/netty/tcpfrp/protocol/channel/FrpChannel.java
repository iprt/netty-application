package io.intellij.netty.tcpfrp.protocol.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

/**
 * FrpChannel
 *
 * @author tech@intellij.io
 * @since 2025-03-06
 */
@Slf4j
public class FrpChannel {
    private final Channel ch;

    public static FrpChannel build(Channel ch) {
        return new FrpChannel(ch);
    }

    private FrpChannel(Channel ch) {
        this.ch = ch;
    }

    public ChannelFuture writeAndFlush(Object msg, ChannelFutureListener... listeners) {
        if (ch.isActive()) {
            return ch.writeAndFlush(msg).addListeners(listeners);
        }
        log.error("Channel is not active(or is null), cannot write and flush message");
        return null;
    }

    public void read() {
        if (ch.isActive()) {
            ch.read();
        } else {
            log.error("Channel is not active(or is null), cannot read message");
        }
    }

}
