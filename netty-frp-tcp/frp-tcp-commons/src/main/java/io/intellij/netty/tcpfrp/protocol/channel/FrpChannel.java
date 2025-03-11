package io.intellij.netty.tcpfrp.protocol.channel;

import io.intellij.netty.tcpfrp.protocol.FrpBasicMsg;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * FrpChannel
 *
 * @author tech@intellij.io
 * @since 2025-03-06
 */
@Slf4j
public class FrpChannel {
    static final AttributeKey<FrpChannel> FRP_CHANNEL_KEY = AttributeKey.valueOf("frpChannel");

    private final Channel ch;

    static void build(Channel ch) {
        FrpChannel frpChannel = new FrpChannel(ch);
        ch.attr(FRP_CHANNEL_KEY).set(frpChannel);
    }

    private FrpChannel(Channel ch) {
        this.ch = ch;
    }

    public Channel get() {
        return ch;
    }

    public ChannelFuture write(DispatchPacket dispatchPacket, ChannelFutureListener... listeners) {
        return this.writeObj(dispatchPacket, listeners);
    }

    public ChannelFuture write(FrpBasicMsg dispatchPacket, ChannelFutureListener... listeners) {
        return this.writeObj(dispatchPacket, listeners);
    }

    public FrpChannel flush() {
        if (ch.isActive()) {
            ch.flush();
        }
        return this;
    }

    public ChannelFuture writeAndFlushEmpty(ChannelFutureListener... listeners) {
        return this.writeAndFlushObj(Unpooled.EMPTY_BUFFER, listeners);
    }

    public ChannelFuture writeAndFlush(DispatchPacket dispatchPacket, ChannelFutureListener... listeners) {
        return this.writeAndFlushObj(dispatchPacket, listeners);
    }

    public ChannelFuture writeAndFlush(FrpBasicMsg basicMsg, ChannelFutureListener... listeners) {
        return this.writeAndFlushObj(basicMsg, listeners);
    }

    private ChannelFuture writeObj(Object msg, ChannelFutureListener... listeners) {
        if (ch.isActive()) {
            return ch.writeAndFlush(msg).addListeners(listeners);
        }
        log.error("Channel is not active(or is null), cannot write message");
        return null;
    }

    private ChannelFuture writeAndFlushObj(Object msg, ChannelFutureListener... listeners) {
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

    public void close() {
        if (ch.isActive()) {
            ch.close();
        }
    }

    public static FrpChannel get(@NotNull Channel ch) {
        return ch.attr(FRP_CHANNEL_KEY).get();
    }

}
