package io.intellij.netty.tcpfrp.client.handlers;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * BootstrapMain
 *
 * @author tech@intellij.io
 */
@Slf4j
public class BootstrapMain {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        // connect to service
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(@NotNull SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
                                log.info("channelActive");

                                Thread.sleep(1000);
                                ctx.close();
                            }
                        });
                    }
                });

        ListeningConfig listeningConfig = ListeningConfig.builder()
                .name("mysql").localIp("172.100.1.100").localPort(33066).build();
        log.info("try connect|{}|{}", listeningConfig.getLocalIp(), listeningConfig.getLocalPort());
        b.connect(listeningConfig.getLocalIp(), listeningConfig.getLocalPort())
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(@NotNull ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            log.info("connect to service {} success", listeningConfig);
                        } else {
                            log.error("connect to service {} failed", listeningConfig);
                        }
                    }
                });
        log.info("after connect ....");

    }
}
