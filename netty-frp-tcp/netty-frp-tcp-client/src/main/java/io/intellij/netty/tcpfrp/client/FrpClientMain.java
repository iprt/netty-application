package io.intellij.netty.tcpfrp.client;

import io.intellij.netty.tcpfrp.SysConfig;
import io.intellij.netty.tcpfrp.client.handlers.FrpClientInitializer;
import io.intellij.netty.tcpfrp.config.ClientConfig;
import io.intellij.netty.tcpfrp.protocol.client.AuthRequest;
import io.intellij.netty.utils.ChannelUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * FrpClientMain
 *
 * @author tech@intellij.io
 */
@Slf4j
public class FrpClientMain {

    public static void main(String[] args) throws InterruptedException {
        ClientConfig clientConfig = ClientConfig.init(ClientConfig.class.getClassLoader().getResourceAsStream("client-config.json"));
        if (clientConfig.isValid()) {
            log.info("client config|{}", clientConfig);
        } else {
            log.error("client config is invalid");
            return;
        }

        SysConfig.logDetails();

        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();

            b.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.AUTO_READ, false)
                    .option(ChannelOption.SO_KEEPALIVE, true);

            b.handler(new FrpClientInitializer(clientConfig));

            String serverHost = clientConfig.getServerHost();
            int serverPort = clientConfig.getServerPort();

            ChannelFuture connect = b.connect(serverHost, serverPort).sync();
            connect.addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    log.info("Connect to frp-server success|host={} |port={}", serverHost, serverPort);

                    log.info("send auth request");
                    Channel channel = f.channel();
                    channel.writeAndFlush(AuthRequest.create(clientConfig.getAuthToken())).addListener(
                            (ChannelFutureListener) f1 -> {
                                if (f1.isSuccess()) {
                                    // for read
                                    f1.channel().pipeline().fireChannelActive();
                                } else {
                                    ChannelUtils.closeOnFlush(f1.channel());
                                }
                            });
                } else {
                    log.error("Connect to frp-server failed|host={} |port={}", serverHost, serverPort);
                }
            });

            connect.channel().closeFuture().sync();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }

    }

}
