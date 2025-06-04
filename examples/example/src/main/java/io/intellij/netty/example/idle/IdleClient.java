package io.intellij.netty.example.idle;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * IdleClient
 *
 * @author tech@intellij.io
 * @since 2025-05-26
 */
@Slf4j
public class IdleClient {

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup(2);

        Bootstrap bootstrap = new Bootstrap();
        try {

            bootstrap.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    log.error("服务端断开了连接|channel.remoteAddr={}", ctx.channel().remoteAddress());
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    log.error("IdleClient exceptionCaught|{}", cause.getMessage());
                                    ctx.close();
                                }
                            });
                        }
                    });

            ChannelFuture connect = bootstrap.connect("127.0.0.1", IdleServer.port);
            connect.addListener(future -> {
                if (future.isSuccess()) {
                    log.info("连接服务端成功|channel.remoteAddr={}", connect.channel().remoteAddress());
                } else {
                    log.error("连接服务端失败|channel.remoteAddr={}", connect.channel().remoteAddress());
                }
            });

            connect.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

}
