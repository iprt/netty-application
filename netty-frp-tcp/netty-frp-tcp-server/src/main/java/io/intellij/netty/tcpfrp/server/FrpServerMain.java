package io.intellij.netty.tcpfrp.server;

import io.intellij.netty.tcpfrp.config.ServerConfig;
import io.intellij.netty.tcpfrp.server.handlers.FrpServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * FrpServerMain
 *
 * @author tech@intellij.io
 */
@Slf4j
public class FrpServerMain {

    public static void main(String[] args) throws InterruptedException {
        ServerConfig serverConfig = ServerConfig.init("");

        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new FrpServerInitializer(serverConfig));

            ChannelFuture f = b.bind(serverConfig.getPort()).sync();

            log.info("frp server start at port:{}", serverConfig.getPort());

            f.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }

}
