package io.intellij.netty.spring.boot.netty.handlers;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * EchoHandler
 *
 * @author tech@intellij.io
 */
@ChannelHandler.Sharable
@Slf4j
public class EchoHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(io.netty.channel.ChannelHandlerContext ctx, Object msg) {
        ctx.write(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Exception caught|{}", cause.getMessage());
    }

}
