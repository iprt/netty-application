package io.intellij.netty.example.idle;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * IdleServer
 *
 * @author tech@intellij.io
 * @since 2025-05-26
 */
@Slf4j
public class IdleServer {

    static int port = 7000;

    public static void main(String[] args) throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup(2);

        ServerBootstrap bootstrap = new ServerBootstrap();

        try {
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();

                            final int readIdleTime = 3; // 读空闲时间，单位秒

                            p.addLast(new IdleStateHandler(readIdleTime, 0, 0, TimeUnit.SECONDS));
                            p.addLast(new ChannelInboundHandlerAdapter() {

                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    log.info("接收到客户端连接|channel.remoteAddr={}", ctx.channel().remoteAddress());
                                }

                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    if (evt instanceof IdleStateEvent) {
                                        // 读超时，关闭连接
                                        log.error("客户端连接{}秒内没有数据，关闭连接|channel.remoteAddr={}", readIdleTime, ctx.channel().remoteAddress());
                                        ctx.close();
                                    } else {
                                        super.userEventTriggered(ctx, evt);
                                    }
                                }
                            });
                        }
                    });

            ChannelFuture bind = bootstrap.bind(port).sync();
            bind.addListener(future -> {
                if (future.isSuccess()) {
                    log.info("Idle Server Started successfully on port {}", port);
                } else {
                    log.error("Idle Server Started failed on port {}", port, future.cause());
                }
            });

            bind.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }

}
