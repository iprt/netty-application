package io.intellij.netty.tcpfrp.server.listening;

import io.intellij.netty.tcpfrp.commons.EventLoopGroups;
import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MultiPortNettyServer
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
@Slf4j
public class MultiPortNettyServer {
    private static final AttributeKey<MultiPortNettyServer> MULTI_PORT_NETTY_SERVER_KEY = AttributeKey.valueOf("multiPortNettyServer");

    private final List<Integer> ports;
    private final FrpChannel frpChannel;

    private final Map<Integer, Channel> SERVER_CHANNEL = new ConcurrentHashMap<>();

    public boolean start() {
        EventLoopGroups container = EventLoopGroups.get();
        EventLoopGroup bossGroup = container.getBossGroup();
        EventLoopGroup workerGroup = container.getWorkerGroup();

        try {
            for (int port : ports) {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childOption(ChannelOption.AUTO_READ, false)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(@NotNull SocketChannel ch) throws Exception {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast(new UserChannelHandler(port, frpChannel));
                            }
                        });

                // 绑定端口并启动服务器
                ChannelFuture channelFuture = b.bind(port).sync();

                SERVER_CHANNEL.put(port, channelFuture.channel());

                log.info("frp-server listening on port {}", port);
            }

            return true;
        } catch (Exception e) {
            log.error("", e);
            this.stop();
            return false;
        }
    }

    public void stop() {
        log.warn("Multi Port Server Stop Begin ...");
        SERVER_CHANNEL.forEach((port, channel) -> {
            log.warn("stopped and release listening port {}", port);
            if (channel != null && channel.isActive()) {
                channel.close();
            }
        });
        log.warn("Multi Port Server Stop End   ...");
    }

    public static void buildIn(@NotNull Channel ch, @NotNull MultiPortNettyServer server) {
        ch.attr(MULTI_PORT_NETTY_SERVER_KEY).set(server);
    }

    public static void stopIn(@NotNull Channel ch) {
        MultiPortNettyServer server = ch.attr(MULTI_PORT_NETTY_SERVER_KEY).get();
        if (server != null) {
            server.stop();
            ch.attr(MULTI_PORT_NETTY_SERVER_KEY).set(null);
        }
    }
}
