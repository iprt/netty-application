package io.intellij.netty.tcpfrp.client;

import io.intellij.netty.tcpfrp.SysConfig;
import io.intellij.netty.tcpfrp.client.handlers.FrpClientInitializer;
import io.intellij.netty.tcpfrp.commons.EventLoopGroups;
import io.intellij.netty.tcpfrp.config.ClientConfig;
import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.intellij.netty.tcpfrp.protocol.client.AuthRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import static io.intellij.netty.tcpfrp.protocol.channel.FrpChannel.FRP_CHANNEL_KEY;

/**
 * FrpClientMain
 *
 * @author tech@intellij.io
 */
@Slf4j
public class FrpClientMain {
    static final String CONFIG_PATH = System.getProperty("config.path", "client-config.json");

    public static void main(String[] args) throws InterruptedException {
        loadConfig().then(FrpClientMain::start);
    }

    static ClientConfig loadConfig() {
        ClientConfig clientConfig = ClientConfig.init(ClientConfig.class.getClassLoader().getResourceAsStream(CONFIG_PATH));
        if (clientConfig.isValid()) {
            log.info("client config|{}", clientConfig);
            SysConfig.get().logDetails();
        } else {
            log.error("client config is invalid");
        }
        return clientConfig;
    }

    static void start(ClientConfig config) {
        EventLoopGroup eventLoopGroup = EventLoopGroups.get().getWorkerGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.AUTO_READ, false)
                    .option(ChannelOption.SO_KEEPALIVE, true);

            b.handler(new FrpClientInitializer(config));

            String serverHost = config.getServerHost();
            int serverPort = config.getServerPort();

            ChannelFuture connect = b.connect(serverHost, serverPort).sync();
            connect.addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.isSuccess()) {
                    log.info("connect to frp-server success|host={} |port={}", serverHost, serverPort);

                    Channel channel = channelFuture.channel();
                    log.info("initialize frp channel");
                    FrpChannel frpChannel = FrpChannel.build(channel);
                    channel.attr(FRP_CHANNEL_KEY).set(frpChannel);

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
