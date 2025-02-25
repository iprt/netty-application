package io.intellij.netty.tcp.lb;

import io.intellij.netty.tcp.lb.config.ConfigParser;
import io.intellij.netty.tcp.lb.config.LbConfig;
import io.intellij.netty.tcp.lb.handlers.LoadBalancerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * TcpLoadBalancer
 *
 * @author tech@intellij.io
 * @since 2025-02-20
 */
@Slf4j
public class TcpLoadBalancer {

    public static void main(String[] args) {
        LbConfig lbConfig = ConfigParser.loadConfig("lb-config.json");
        if (Objects.isNull(lbConfig)) {
            log.error("load config error");
            return;
        } else {
            log.info("load config success|config: {}", lbConfig);
        }

        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();

        try {
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new LoadBalancerInitializer(lbConfig.getLbStrategy(), lbConfig.getBackends()))
                    .childOption(ChannelOption.AUTO_READ, false);

            ChannelFuture sync = bootstrap.bind(lbConfig.getPort()).sync();
            log.info("TcpLoadBalancer start on port: {}", lbConfig.getPort());
            sync.channel().closeFuture().sync();

        } catch (Exception e) {
            log.error("TcpLoadBalancer start error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
