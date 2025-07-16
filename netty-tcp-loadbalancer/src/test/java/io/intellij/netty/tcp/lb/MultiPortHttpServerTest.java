package io.intellij.netty.tcp.lb;

import com.alibaba.fastjson2.JSON;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;

/**
 * MultiPortHttpServerTest
 *
 * @author tech@intellij.io
 * @since 2025-02-24
 */
public class MultiPortHttpServerTest {

    @Test
    public void running() throws Exception {
        List<Integer> ports = List.of(8081, 8082, 8083);

        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup(2);

        final Lock lock = new ReentrantLock();
        // Condition to signal shutdown
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
                .childHandler(
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline()
                                        .addLast(new HttpServerCodec())
                                        .addLast(new HttpResponseHandler(port, lock, condition));
                            }
                        }
                );

        System.out.println("http server start on port: " + port);
        b.bind(port).sync();

    }

    @RequiredArgsConstructor
    static class HttpResponseHandler extends SimpleChannelInboundHandler<HttpObject> {
        static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        private final int port;
        private final Lock lock;
        private final Condition shutdownCondition;

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) throws Exception {
            if (httpObject instanceof HttpRequest req) {
                String msg = JSON.toJSONString(Map.of("time", LocalDateTime.now().format(TIME_FORMATTER), "port", port));
                byte[] bytes = msg.getBytes();
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        Unpooled.copiedBuffer(bytes)
                );
                response.headers()
                        .set(CONTENT_TYPE, APPLICATION_JSON)
                        .set(CONTENT_LENGTH, Integer.toString(bytes.length));

                ctx.write(response)
                        .addListener(ChannelFutureListener.CLOSE);
                // 获取 req 的uri
                String uri = req.uri();
                if ("/shutdown".equals(uri)) {
                    lock.lock();
                    try {
                        shutdownCondition.signalAll();
                    } finally {
                        lock.unlock();
                    }
                }
            }
        }
    }

}