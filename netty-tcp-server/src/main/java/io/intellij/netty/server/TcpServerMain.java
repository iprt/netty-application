package io.intellij.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * TcpServerMain
 *
 * @author tech@intellij.io
 */
@Slf4j
public class TcpServerMain {
    // -Dport=8080
    // static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));
    static int PORT = 8080;

    public static void main(String[] args) {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(4);

        ServerBootstrap b = new ServerBootstrap();
        try {
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ServerInitializer());
            b.bind(PORT).sync().channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    static {
        String envPortStr = System.getenv("SERVER_PORT");
        try {
            if (StringUtils.isNotBlank(envPortStr)) {
                int envPort = Integer.parseInt(envPortStr);
                if (envPort > 0 && envPort < 65535) {
                    PORT = envPort;
                }
            }
        } catch (NumberFormatException e) {
            log.error(e.getMessage());
        }
    }

}
