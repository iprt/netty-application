package io.intellij.netty.tcpfrp.server;

import io.intellij.netty.tcpfrp.commons.EventLoopGroups;
import io.intellij.netty.tcpfrp.config.ServerConfig;
import io.intellij.netty.tcpfrp.server.handlers.FrpServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * FrpServer
 *
 * @author tech@intellij.io
 * @since 2025-03-08
 */
@Slf4j
final class FrpServer {
    static void startServer(ServerConfig config) {
        EventLoopGroups container = EventLoopGroups.get();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(container.getBossGroup(), container.getWorkerGroup())
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.AUTO_READ, false)
                    .childHandler(new FrpServerInitializer(config));

            ChannelFuture f = b.bind(config.getPort()).sync();
            f.addListener((ChannelFutureListener) cf -> {
                if (cf.isSuccess()) {
                    log.info("frp server started on port {}", config.getPort());
                } else {
                    log.error("frp server start failed", cf.cause());
                }
            });
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("frp server start failed", e);
        } finally {
            container.getBossGroup().shutdownGracefully();
            container.getWorkerGroup().shutdownGracefully();
        }
    }
}
