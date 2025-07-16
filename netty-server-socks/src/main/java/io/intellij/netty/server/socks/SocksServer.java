package io.intellij.netty.server.socks;

import io.intellij.netty.server.socks.handlers.socks5auth.Authenticator;
import io.intellij.netty.server.socks.handlers.socks5auth.PasswordAuthenticator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import static io.intellij.netty.server.socks.config.Properties.PORT;

/**
 * SocksServer
 *
 * @author tech@intellij.io
 */
@Slf4j
public class SocksServer {

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        Authenticator authenticator = new PasswordAuthenticator();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new SocksServerInitializer(authenticator));
            ChannelFuture sync = b.bind(PORT).sync();
            log.info("Socks server started on port {}", PORT);
            sync.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
