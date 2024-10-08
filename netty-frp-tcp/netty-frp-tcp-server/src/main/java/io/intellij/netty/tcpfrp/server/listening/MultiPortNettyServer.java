package io.intellij.netty.tcpfrp.server.listening;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.server.thread.ThreadPool;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MultiPortNettyServer
 *
 * @author tech@intellij.io
 */
@Slf4j
public class MultiPortNettyServer {
    private final Map<Integer, ListeningConfig> portToServer;
    private final Channel exchangeChannel;

    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    public MultiPortNettyServer(Map<String, ListeningConfig> listeningConfigMap, Channel exchangeChannel) {
        this.exchangeChannel = exchangeChannel;
        this.portToServer = listeningConfigMap.values().stream()
                .collect(Collectors.toMap(ListeningConfig::getRemotePort, Function.identity()));
    }

    public boolean start() {
        try {
            for (Map.Entry<Integer, ListeningConfig> e : portToServer.entrySet()) {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childOption(ChannelOption.AUTO_READ, false)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(@NotNull SocketChannel ch) throws Exception {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast(new UserHandler(portToServer, exchangeChannel));
                            }
                        });

                // 绑定端口并启动服务器
                b.bind(e.getKey()).sync();
                log.info("{} service started and listening on port {}", e.getValue().getName(), e.getKey());
            }

            ThreadPool.ES.execute(() -> {
                try {
                    // 等待直到所有的端口都绑定完成并开始接受连接
                    // 阻塞
                    bossGroup.terminationFuture().sync();
                } catch (InterruptedException e) {
                    log.error("", e);
                }
            });

            return true;

        } catch (Exception e) {
            log.error("", e);
            this.stop();
            return false;
        }
    }

    public void stop() {
        log.warn("multi port server stop ...");
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

}
