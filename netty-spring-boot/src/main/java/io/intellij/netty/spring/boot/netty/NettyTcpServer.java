package io.intellij.netty.spring.boot.netty;

import io.intellij.netty.spring.boot.entities.NettyServerConf;
import io.intellij.netty.spring.boot.entities.NettySeverRunRes;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
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

    @Override
    public synchronized NettySeverRunRes start(NettyServerConf conf) {
        if (PORTS.containsKey(conf.getPort())) {
            log.warn("NettyTcpServer already running on port: {}", conf.getPort());
            return NettySeverRunRes.builder()
                    .status(false)
                    .msg("NettyTcpServer already running on port: " + conf.getPort())
                    .build();
        }

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
                                int i = byteBuf.readableBytes();
                                byte[] bytes = new byte[i];
                                byteBuf.readBytes(bytes);
                                log.info("Received: {}", new String(bytes));
                            }
                        });
                    }
                });
        try {
            ChannelFuture bind = bootstrap.bind(conf.getPort());
            bind.addListener(future -> {
                if (future.isSuccess()) {
                    log.info("NettyTcpServer started on port: {}", conf.getPort());
                } else {
                    log.error("NettyTcpServer start failed", future.cause());
                }
            });
            ChannelFuture channelFuture = bind.sync();
            PORTS.put(conf.getPort(), channelFuture);
            log.info("NettyTcpServer started on port: {}", conf.getPort());
            return NettySeverRunRes.builder()
                    .status(true)
                    .msg("NettyTcpServer started on port: " + conf.getPort())
                    .build();
        } catch (Exception e) {
            log.error("NettyTcpServer start failed", e);
            return NettySeverRunRes.builder()
                    .status(false)
                    .msg("NettyTcpServer start failed: " + e.getMessage())
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
