package io.intellij.netty.tcpfrp.client.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Promise;
import org.jetbrains.annotations.NotNull;

/**
 * DirectServiceHandler
 *
 * @author tech@intellij.io
 */
public class DirectServiceHandler extends ChannelInboundHandlerAdapter {

    private final Promise<Channel> promise;

    public DirectServiceHandler(Promise<Channel> promise) {
        this.promise = promise;
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) {
        ctx.pipeline().remove(this);
        promise.setSuccess(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        promise.setFailure(throwable);
    }

}
