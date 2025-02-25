package io.intellij.netty.tcp.lb;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
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
        Condition condition = lock.newCondition();

        try {
            for (Integer port : ports) {
                start(port, boss, worker, lock, condition);
            }
            lock.lock();
            try {
                // curl localhost:8081/shutdown stop all server
                condition.await();
            } finally {
                lock.unlock();
            }

        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }

    void start(int port, EventLoopGroup boss, EventLoopGroup worker, Lock lock, Condition condition) throws Exception {
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

}
