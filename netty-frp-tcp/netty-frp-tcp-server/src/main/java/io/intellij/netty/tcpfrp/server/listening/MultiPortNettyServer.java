package io.intellij.netty.tcpfrp.server.listening;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.intellij.netty.tcpfrp.server.EventLoopGroupContainer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MultiPortNettyServer
 *
 * @author tech@intellij.io
 */
@Slf4j
public class MultiPortNettyServer {
    private final Map<Integer, ListeningConfig> portToListeningConfig;
    private final FrpChannel frpChannel;

    private final Map<Integer, Channel> SERVER_CHANNEL = new ConcurrentHashMap<>();

    public MultiPortNettyServer(@NotNull Map<String, ListeningConfig> listeningConfigMap, Channel frpChannel) {
        this.frpChannel = FrpChannel.build(frpChannel);
        this.portToListeningConfig = listeningConfigMap.values().stream()
                .collect(Collectors.toMap(ListeningConfig::getRemotePort, Function.identity()));
    }

    public boolean start() {
        EventLoopGroupContainer container = EventLoopGroupContainer.get();
        EventLoopGroup bossGroup = container.getBossGroup();
        EventLoopGroup workerGroup = container.getWorkerGroup();

        try {
            for (Map.Entry<Integer, ListeningConfig> e : portToListeningConfig.entrySet()) {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childOption(ChannelOption.AUTO_READ, false)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(@NotNull SocketChannel ch) throws Exception {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast(new UserChannelHandler(portToListeningConfig, frpChannel));
                            }
                        });

                // 绑定端口并启动服务器
                ChannelFuture channelFuture = b.bind(e.getKey()).sync();

                SERVER_CHANNEL.put(e.getKey(), channelFuture.channel());

                log.info("service <{}> started and listening on port {}", e.getValue().getName(), e.getKey());
            }

            return true;
        } catch (Exception e) {
            log.error("", e);
            this.stop();
            return false;
        }
    }

    public void stop() {
        log.warn("Multi Port Server Stop begin ...");
        SERVER_CHANNEL.forEach((port, channel) -> {
            ListeningConfig listeningConfig = portToListeningConfig.get(port);
            log.warn("service <{}> stopped and release listening port {}", listeningConfig.getName(), port);
            if (channel != null && channel.isActive()) {
                channel.close();
            }
        });
        log.warn("Multi Port Server Stop end   ...");
    }

}
