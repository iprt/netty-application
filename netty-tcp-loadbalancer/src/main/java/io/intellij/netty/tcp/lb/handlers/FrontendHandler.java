package io.intellij.netty.tcp.lb.handlers;

import io.intellij.netty.tcp.lb.config.Backend;
import io.intellij.netty.tcp.lb.config.LbStrategy;
import io.intellij.netty.tcp.lb.strategy.BackendChooser;
import io.intellij.netty.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * FrontHandler
 *
 * @author tech@intellij.io
 * @since 2025-02-20
 */
@RequiredArgsConstructor
@Slf4j
public class FrontendHandler extends ChannelInboundHandlerAdapter {
    private final LbStrategy strategy;
    private final Map<String, Backend> backends;
    private final AtomicReference<Channel> outRef = new AtomicReference<>(null);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel inboundChannel = ctx.channel();

        BackendChooser chooser = BackendChooser.get(strategy, backends);
        BootstrapLoop loop = new BootstrapLoop(chooser, inboundChannel, outRef);
        loop.connect();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel outboundChannel = outRef.get();
        if (outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(msg).addListener(
                    (ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            // was able to flush out data, start to read the next chunk
                            // 切入点
                            ctx.channel().read();
                        } else {
                            future.channel().close();
                        }
                    }
            );
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("FrontendHandler error", cause);
        ChannelUtils.closeOnFlush(ctx.channel());
    }

}
