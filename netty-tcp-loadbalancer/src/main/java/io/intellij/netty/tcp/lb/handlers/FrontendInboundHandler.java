package io.intellij.netty.tcp.lb.handlers;

import io.intellij.netty.tcp.lb.config.Backend;
import io.intellij.netty.tcp.lb.config.LbStrategy;
import io.intellij.netty.tcp.lb.strategy.BackendChooser;
import io.intellij.netty.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * FrontendInboundHandler
 *
 * @author tech@intellij.io
 * @since 2025-02-20
 */
@RequiredArgsConstructor
@Slf4j
public class FrontendInboundHandler extends ChannelInboundHandlerAdapter {

    static final AttributeKey<Channel> OUTBOUND_CHANNEL_KEY = AttributeKey.newInstance("outboundChannel");

    private final LbStrategy strategy;
    private final Map<String, Backend> backends;

    // private final AtomicReference<Channel> outRef = new AtomicReference<>(null);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel inboundChannel = ctx.channel();

        BackendChooser chooser = BackendChooser.get(strategy, backends);
        BootstrapLoopConnector loop = new BootstrapLoopConnector(chooser, inboundChannel);
        loop.connect();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel inbound = ctx.channel();
        Channel outbound = inbound.attr(OUTBOUND_CHANNEL_KEY).get();
        if (outbound.isActive()) {
            outbound.writeAndFlush(msg).addListener(
                    (ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            // was able to flush outbound data, start to read the next chunk
                            // 切入点
                            inbound.read();
                        } else {
                            future.channel().close();
                        }
                    }
            );
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // client close the connection
        Channel outboundChannel = ctx.channel().attr(OUTBOUND_CHANNEL_KEY).get();
        ChannelUtils.closeOnFlush(outboundChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("FrontendHandler error", cause);
        ChannelUtils.closeOnFlush(ctx.channel());
    }

}
