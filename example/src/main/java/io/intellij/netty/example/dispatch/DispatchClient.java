package io.intellij.netty.example.dispatch;

import io.intellij.netty.example.dispatch.handlers.client.ClientInitializer;
import io.intellij.netty.example.dispatch.model.HeartBeat;
import io.intellij.netty.example.dispatch.model.msg.LoginReq;
import io.intellij.netty.example.dispatch.model.msg.LogoutReq;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * DispatchClient
 *
 * @author tech@intellij.io
 */
@Slf4j
public class DispatchClient {
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final Bootstrap bootstrap = new Bootstrap();

    private final String host;
    private final int port;

    private Channel channel;

    public boolean start() {
        ChannelFuture future = bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ClientInitializer())
                .connect(host, port)
                .addListener((ChannelFutureListener) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        channel = channelFuture.channel();
                    } else {
                        log.error("connect failed |{}", channelFuture.cause().getMessage());
                        stop();
                    }
                });

        try {
            future.sync();
            log.info("client started|connect to {}:{}", host, port);
            return true;
        } catch (InterruptedException e) {
            return false;
        }

    }

    public void stop() {
        log.warn("client stop");
        group.shutdownGracefully();
    }

    public <T> void send(T msg) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(msg);
        }
    }

    public DispatchClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) throws InterruptedException {
        DispatchClient dispatchClient = new DispatchClient("127.0.0.1", DispatchServer.PORT);
        if (dispatchClient.start()) {
            ScheduledExecutorService ses = Executors.newScheduledThreadPool(3);
            Random random = new Random();
            ses.scheduleAtFixedRate(() -> {
                dispatchClient.send(HeartBeat.builder()
                        .time(new Date()).id("client")
                        .seq(getRandomLongInRange(1, 1000))
                        .build());
            }, 0, 3, TimeUnit.SECONDS);

            ses.scheduleAtFixedRate(() -> {
                dispatchClient.send(LoginReq.create("admin", "admin"));
                dispatchClient.send(LogoutReq.create("admin"));
            }, 0, 5, TimeUnit.SECONDS);

            ses.schedule(() -> {
                dispatchClient.stop();
                ses.shutdown();
            }, 15, TimeUnit.SECONDS);

        }
    }

    public static long getRandomLongInRange(long min, long max) {
        Random random = new Random();
        return min + (long) (random.nextDouble() * (max - min));
    }
}
