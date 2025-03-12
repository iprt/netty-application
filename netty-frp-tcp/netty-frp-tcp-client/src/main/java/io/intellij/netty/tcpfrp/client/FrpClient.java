package io.intellij.netty.tcpfrp.client;

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
    private final Bootstrap b = new Bootstrap();
    private final EventLoopGroup eventLoopGroup = EventLoopGroups.get().getWorkerGroup(2);
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

    void start() {
        doStart(0);
    }

    private void doStart(int count) {
        String serverHost = config.getServerHost();
        int serverPort = config.getServerPort();
        ChannelFuture connectFuture = b.connect(serverHost, serverPort);

        connectFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("[CONNECT] connect to frp-server success|host={} |port={}", serverHost, serverPort);
                Channel ch = future.channel();
                FrpChannel frpChannel = FrpChannel.getBy(ch);
                log.info("Send Auth Request");
                frpChannel.writeAndFlush(AuthRequest.create(config.getAuthToken()), f -> {
                    if (f.isSuccess()) {
                        // for read
                        f.channel().pipeline().fireChannelActive();
                    }
                });

                ChannelFuture closeFuture = ch.closeFuture();
                if (reconnect) {
                    // detect channel close then restart
                    closeFuture.addListener((ChannelFutureListener) detectFuture -> {
                        eventLoopGroup.execute(() -> this.doStart(0));
                    });
                }
            } else if (reconnect) {
                eventLoopGroup.schedule(() -> {
                    log.warn("[RECONNECT] reconnect to frp-server <{}:{}> | count={}", serverHost, serverPort, count);
                    this.doStart(count + 1);
                }, 3, TimeUnit.SECONDS);
            } else {
                log.error("Connect to frp-server <{}:{}> failed.", serverHost, serverPort);
                log.error("Exit...");
                this.stop();
            }
        });

        ChannelFuture closeFuture = connectFuture.channel().closeFuture();
        try {
            closeFuture.sync();
            log.error("[RECONNECT] lost connection to frp-server | count={}", count);
        } catch (InterruptedException e) {
            log.error("closeFuture.sync()|errorMsg={}", e.getMessage());
        } finally {
            if (!reconnect) {
                this.stop();
            }
        }
    }

    void stop() {
        log.warn("eventLoopGroup.shutdownGracefully()...");
        eventLoopGroup.shutdownGracefully();
    }

    static void doStart(ClientConfig config) {
        new FrpClient(config, false).start();
    }

    static void startReconnect(ClientConfig config) {
        new FrpClient(config, true).start();
    }

}
