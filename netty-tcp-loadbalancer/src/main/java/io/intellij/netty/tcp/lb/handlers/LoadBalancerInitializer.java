package io.intellij.netty.tcp.lb.handlers;

import io.intellij.netty.tcp.lb.config.Backend;
import io.intellij.netty.tcp.lb.config.LbStrategy;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * LoadBalancerInitializer
 *
 * @author tech@intellij.io
 * @since 2025-02-20
 */
@RequiredArgsConstructor
public class LoadBalancerInitializer extends ChannelInitializer<SocketChannel> {
    private final LbStrategy strategy;
    private final Map<String, Backend> backends;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new FrontendHandler(strategy, backends));
    }

}
