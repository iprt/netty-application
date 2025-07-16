package io.intellij.netty.tcp.lb;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.ReferenceCountUtil;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * MultiPortEchoServerTest
 *
 * @author tech@intellij.io
 * @since 2025-02-25
 */
public class MultiPortEchoServerTest {

    @Test
    public void running() throws Exception {
        List<Integer> ports = List.of(8081, 8082, 8083);

        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup(2);

        final Lock lock = new ReentrantLock();
        Condition shutdownCondition = lock.newCondition();

        try {
            for (Integer port : ports) {
                startServer(port, boss, worker, lock, shutdownCondition);
            }
            lock.lock();
            try {
                // curl localhost:8081/shutdown stop all server
                shutdownCondition.await();
            } finally {
                lock.unlock();
            }
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }

    void startServer(int port, EventLoopGroup boss, EventLoopGroup worker, Lock lock, Condition condition) throws Exception {
        ServerBootstrap b = new ServerBootstrap();

        b.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new StringEncoder())
                                .addLast(new EchoResponseHandler(port, lock, condition));
                    }
                });


        System.out.println("echo server start on port: " + port);
        b.bind(port).sync();
    }


    @RequiredArgsConstructor
    static class EchoResponseHandler extends ChannelInboundHandlerAdapter {
        private final int port;
        private final Lock lock;
        private final Condition shutdownCondition;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof ByteBuf byteBuf) {
                int i = byteBuf.readableBytes();
                byte[] bytes = new byte[i];
                byteBuf.readBytes(bytes);

                String response = new String(bytes);
                ChannelFuture future = ctx.writeAndFlush(response + "-" + port);
                if ("shutdown".equals(response)) {
                    future.addListener(ChannelFutureListener.CLOSE);
                    lock.lock();
                    try {
                        shutdownCondition.signalAll();
                    } finally {
                        lock.unlock();
                    }
                }
            }
            ReferenceCountUtil.release(msg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("Channel inactive: " + ctx.channel().remoteAddress());
        }
    }

}
