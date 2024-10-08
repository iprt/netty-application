package io.intellij.netty.server.tcpproxy;

import io.intellij.netty.server.tcpproxy.handler.HexDumpProxyInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * HexDumpProxy
 *
 * @author tech@intellij.io
 */
@Slf4j
public final class HexDumpProxy {

    static String LOCAL_HOST;
    static int LOCAL_PORT;
    static String REMOTE_HOST;
    static int REMOTE_PORT;

    public static void main(String[] args) throws Exception {

        readProperties();

        // InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
        System.err.println("Proxying *:" + LOCAL_PORT + " to " + REMOTE_HOST + ':' + REMOTE_PORT + " ...");

        EventLoopGroup boosGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(boosGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HexDumpProxyInitializer(REMOTE_HOST, REMOTE_PORT))
                    .childOption(ChannelOption.AUTO_READ, false)
                    .bind(LOCAL_HOST, LOCAL_PORT).sync().channel().closeFuture().sync();
        } finally {
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    static void readProperties() throws Exception {
        if (new File("config.properties").exists()) {
            log.info("read config file");

            Properties p = new Properties();
            p.load(new FileInputStream("config.properties"));
            LOCAL_HOST = p.getProperty("localHost", "127.0.0.1");
            LOCAL_PORT = Integer.parseInt(p.getProperty("localPort", "3306"));
            REMOTE_HOST = p.getProperty("remoteHost", "172.100.1.100");
            REMOTE_PORT = Integer.parseInt(p.getProperty("remotePort", "3306"));

        } else {
            LOCAL_HOST = System.getProperty("localHost", "0.0.0.0");
            LOCAL_PORT = Integer.parseInt(System.getProperty("localPort", "3306"));
            REMOTE_HOST = System.getProperty("remoteHost", "172.100.1.100");
            REMOTE_PORT = Integer.parseInt(System.getProperty("remotePort", "3306"));
        }
    }

}
