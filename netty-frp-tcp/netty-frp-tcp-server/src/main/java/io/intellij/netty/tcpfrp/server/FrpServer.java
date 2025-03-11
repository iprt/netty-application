package io.intellij.netty.tcpfrp.server;

import io.intellij.netty.tcpfrp.commons.EventLoopGroups;
import io.intellij.netty.tcpfrp.config.ServerConfig;
import io.intellij.netty.tcpfrp.server.handlers.FrpServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
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
    private final ServerBootstrap b = new ServerBootstrap();
    private final EventLoopGroup boss = EventLoopGroups.get().getBossGroup();
    private final EventLoopGroup worker = EventLoopGroups.get().getWorkerGroup();
    private final ServerConfig config;

    FrpServer(ServerConfig config) {
        this.config = config;
        b.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new FrpServerInitializer(config));
    }

    void start() {
        try {
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
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    static void start(ServerConfig config) {
        new FrpServer(config).start();
    }

}
