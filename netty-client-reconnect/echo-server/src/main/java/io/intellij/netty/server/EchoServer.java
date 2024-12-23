package io.intellij.netty.server;

import io.intellij.netty.utils.CtxUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * EchoServer
 *
 * @author tech@intellij.io
 */
@Slf4j
public class EchoServer {

    public static void main(String[] args) {
        final int port = 8082;
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();

            b.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(@NotNull SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new ChannelInboundHandlerAdapter() {

                                        @Override
                                        public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
                                            log.info("channelActive|{}", CtxUtils.getRemoteAddress(ctx));
                                        }

                                        @Override
                                        public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
                                            ctx.write(msg);
                                        }

                                        @Override
                                        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                                            ctx.flush();
                                        }

                                        @Override
                                        public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
                                            log.error("channelInactive|localAddress={}|remoteAddress={}", CtxUtils.getLocalAddress(ctx), CtxUtils.getRemoteAddress(ctx));

                                        }

                                        @Override
                                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                            log.error("localAddress={}|remoteAddress={}", CtxUtils.getLocalAddress(ctx), CtxUtils.getRemoteAddress(ctx), cause);
                                        }
                                    }
                            );
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            log.info("echo server start and listen at port:{}", port);
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("echo server", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
