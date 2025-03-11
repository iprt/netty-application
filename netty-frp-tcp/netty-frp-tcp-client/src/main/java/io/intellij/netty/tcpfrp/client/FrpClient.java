package io.intellij.netty.tcpfrp.client;

import io.intellij.netty.tcpfrp.client.handlers.FrpClientInitializer;
import io.intellij.netty.tcpfrp.commons.EventLoopGroups;
import io.intellij.netty.tcpfrp.config.ClientConfig;
import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.intellij.netty.tcpfrp.protocol.client.AuthRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * io.intellij.netty.tcpfrp.client.FrpClient
 *
 * @author tech@intellij.io
 * @since 2025-03-08
 */
@Slf4j
public class FrpClient {

    static void start(ClientConfig config) {
        EventLoopGroup eventLoopGroup = EventLoopGroups.get().getWorkerGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true);

            b.handler(new FrpClientInitializer(config));

            String serverHost = config.getServerHost();
            int serverPort = config.getServerPort();

            ChannelFuture connect = b.connect(serverHost, serverPort).sync();
            connect.addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.isSuccess()) {
                    log.info("connect to frp-server success|host={} |port={}", serverHost, serverPort);
                    FrpChannel frpChannel = FrpChannel.get(channelFuture.channel());

                    log.info("send auth request");
                    frpChannel.writeAndFlush(AuthRequest.create(config.getAuthToken()), f -> {
                        if (f.isSuccess()) {
                            // for read
                            f.channel().pipeline().fireChannelActive();
                        } else {
                            frpChannel.close();
                        }
                    });
                } else {
                    log.error("Connect to frp-server failed|host={} |port={}", serverHost, serverPort);
                }
            });
            connect.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("start frp client failed", e);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }

    }

}
