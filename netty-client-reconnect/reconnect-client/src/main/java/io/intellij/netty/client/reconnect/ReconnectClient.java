package io.intellij.netty.client.reconnect;

import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ReconnectClient
 *
 * @author tech@intellij.io
 */
@Slf4j
public class ReconnectClient {

    public static void main(String[] args) {

        ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();

        Connector connector = new Connector("127.0.0.1", 9001, bootstrap -> {
            NioEventLoopGroup worker = new NioEventLoopGroup(1);
            bootstrap.group(worker)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ClientInitializer());

        });

        connector.connect();

        s.scheduleAtFixedRate(() -> {
            Channel linkChannel = connector.getLinkChannel();
            if (linkChannel != null && linkChannel.isActive()) {
                String uuid = UUID.randomUUID().toString();
                log.info("write msg|{}", uuid);
                linkChannel.writeAndFlush(uuid);
            }
        }, 1, 3, TimeUnit.SECONDS);


    }
}
