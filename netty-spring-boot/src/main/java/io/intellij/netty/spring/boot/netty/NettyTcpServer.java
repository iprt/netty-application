package io.intellij.netty.spring.boot.netty;

import io.intellij.netty.spring.boot.entities.NettyServerConf;
import io.intellij.netty.spring.boot.entities.NettySeverRunRes;
import io.intellij.netty.utils.ServerSocketUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NettyTcpServer
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
@Slf4j
public class NettyTcpServer implements NettyServer {
    private static final Map<Integer, ChannelFuture> PORTS = new ConcurrentHashMap<>();
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final Map<String, ChannelHandler> channelHandlerMap;

    @Override
    public synchronized NettySeverRunRes start(NettyServerConf conf) {
        int port = conf.getPort();
        if (PORTS.containsKey(port)) {
            log.warn("NettyTcpServer already running on port: {}", port);
            return NettySeverRunRes.builder()
                    .status(false)
                    .msg("NettyTcpServer already running on port: " + port)
                    .build();
        }

        if (ServerSocketUtils.isPortInUse(port)) {
            log.error("Port {} is already in use", port);
            return NettySeverRunRes.builder()
                    .status(false)
                    .msg("Port " + port + " is already in use")
                    .build();
        }

        String key = conf.getHandlerKey();
        ChannelHandler channelHandler = channelHandlerMap.get(key);

        if (Objects.isNull(channelHandler)) {
            log.error("ChannelHandler not found for key: {}", key);
            return NettySeverRunRes.builder()
                    .status(false)
                    .msg("ChannelHandler not found for key: " + key)
                    .build();
        }

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(channelHandler);
        try {
            ChannelFuture bind = bootstrap.bind(port);
            bind.addListener(future -> {
                if (future.isSuccess()) {
                    log.info("NettyTcpServer(key={}) started on port: {}", key, port);
                } else {
                    log.error("NettyTcpServer(key={}) start failed", key, future.cause());
                }
            });
            ChannelFuture channelFuture = bind.sync();
            PORTS.put(port, channelFuture);
            log.info("NettyTcpServer(key={}) started on port: {}", key, port);
            return NettySeverRunRes.builder()
                    .status(true)
                    .msg("NettyTcpServer(key={" + key + "}) started on port: " + port)
                    .build();
        } catch (Exception e) {
            log.error("NettyTcpServer(key={}) start failed", key, e);
            return NettySeverRunRes.builder()
                    .status(false)
                    .msg("NettyTcpServer(key={" + key + "}) start failed: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public synchronized boolean isRunning(int port) {
        return PORTS.containsKey(port);
    }

    @Override
    public synchronized void stop(int port) {
        ChannelFuture future = PORTS.get(port);
        if (Objects.isNull(future)) {
            log.warn("NettyTcpServer not running on port: {}", port);
            return;
        }
        try {
            future.channel().close().sync();
        } catch (InterruptedException e) {
            log.error("NettyTcpServer stop failed", e);
        } finally {
            PORTS.remove(port);
        }
    }

}
