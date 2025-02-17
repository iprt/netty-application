package io.intellij.netty.tcpfrp.server;

import io.intellij.netty.tcpfrp.config.ServerConfig;
import io.intellij.netty.tcpfrp.exchange.SysConfig;
import io.intellij.netty.tcpfrp.server.handlers.FrpServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
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
        ServerConfig serverConfig = ServerConfig.init(ServerConfig.class.getClassLoader().getResourceAsStream("server-config.json"));
        if (serverConfig.isValid()) {
            log.info("server config|{}", serverConfig);
        } else {
            log.error("server config is invalid");
            return;
        }
        SysConfig.logDetails();

        EventLoopGroupContainer container = EventLoopGroupContainer.get();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(container.getBossGroup(), container.getWorkerGroup())
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new FrpServerInitializer(serverConfig));

            ChannelFuture f = b.bind(serverConfig.getPort()).sync();

            log.info("frp server start at port:{}", serverConfig.getPort());

            f.channel().closeFuture().sync();
        } finally {
            container.getBossGroup().shutdownGracefully();
            container.getWorkerGroup().shutdownGracefully();
        }
    }

}
