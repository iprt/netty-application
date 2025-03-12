package io.intellij.netty.tcpfrp.client;

import io.intellij.netty.tcpfrp.client.handlers.FrpClientInitializer;
import io.intellij.netty.tcpfrp.commons.EventLoopGroups;
import io.intellij.netty.tcpfrp.config.ClientConfig;
import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.intellij.netty.tcpfrp.protocol.client.AuthRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * FrpClient
 *
 * @author tech@intellij.io
 * @since 2025-03-08
 */
@Slf4j
public class FrpClient {
    private static final AttributeKey<FrpClient> FRP_CLIENT_KEY = AttributeKey.valueOf("frp_client");
    private final Bootstrap b = new Bootstrap();
    private final EventLoopGroup eventLoopGroup = EventLoopGroups.get().getWorkerGroup();
    private final ClientConfig config;
    @Getter
    private final boolean reconnect;

    private FrpClient(ClientConfig config, boolean reconnect) {
        this.config = config;
        this.reconnect = reconnect;
        b.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new FrpClientInitializer(config));
    }

    public void start() {
        String serverHost = config.getServerHost();
        int serverPort = config.getServerPort();
        b.connect(serverHost, serverPort).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("[CONNECT] connect to frp-server success|host={} |port={}", serverHost, serverPort);
                Channel ch = future.channel();
                FrpChannel frpChannel = FrpChannel.get(ch);

                log.info("init frp client in channel");
                ch.attr(FRP_CLIENT_KEY).set(this);

                log.info("Send Auth Request");
                frpChannel.writeAndFlush(AuthRequest.create(config.getAuthToken()), f -> {
                    if (f.isSuccess()) {
                        // for read
                        f.channel().pipeline().fireChannelActive();
                    } else {
                        frpChannel.close();
                    }
                });
            } else {
                if (reconnect) {
                    log.warn("[RECONNECT] reconnect to frp-server<{}:{}>", serverHost, serverPort);
                    eventLoopGroup.schedule(() -> start(), 3, TimeUnit.SECONDS);
                } else {
                    log.error("Connect to frp-server<{}:{}> failed.", serverHost, serverPort);
                    log.error("Exit...");
                    this.stop();
                }
            }
        });
    }

    public void stop() {
        log.warn("eventLoopGroup.shutdownGracefully()...");
        eventLoopGroup.shutdownGracefully();
    }

    static void start(ClientConfig config) {
        new FrpClient(config, false).start();
    }

    static void startReconnect(ClientConfig config) {
        new FrpClient(config, true).start();
    }

    public static FrpClient get(Channel channel) {
        return channel.attr(FRP_CLIENT_KEY).get();
    }

}
