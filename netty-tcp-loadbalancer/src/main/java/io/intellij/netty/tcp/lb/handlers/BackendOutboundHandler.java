package io.intellij.netty.tcp.lb.handlers;

import io.intellij.netty.tcp.lb.config.Backend;
import io.intellij.netty.tcp.lb.strategy.BackendChooser;
import io.intellij.netty.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * BackendOutboundHandler
 *
 * @author tech@intellij.io
 * @since 2025-02-20
 */
@RequiredArgsConstructor
public class BackendOutboundHandler extends ChannelInboundHandlerAdapter {
    private final Channel inboundChannel;

    private final BackendChooser chooser;
    private final Backend target;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        chooser.active(target);
        ctx.read();
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        Channel outbound = ctx.channel();
        inboundChannel.writeAndFlush(msg).addListener(
                (ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        outbound.read();
                    } else {
                        future.channel().close();
                    }
                }
        );
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        // server close the connection
        chooser.inactive(target);
        ChannelUtils.closeOnFlush(inboundChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // cause.printStackTrace();
        ChannelUtils.closeOnFlush(ctx.channel());
    }

}
