package io.intellij.netty.tcp.lb.handlers;

import io.intellij.netty.tcp.lb.config.Backend;
import io.intellij.netty.tcp.lb.strategy.BackendChooser;
import io.intellij.netty.utils.ChannelUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

/**
 * BootstrapLoop
 *
 * @author tech@intellij.io
 * @since 2025-02-24
 */
@RequiredArgsConstructor
@Slf4j
public class BootstrapLoop {
    private final Bootstrap b = new Bootstrap();
    private final BackendChooser chooser;
    private final Channel inboundChannel;
    private final AtomicReference<Channel> outRef;

    public void connect() {
        b.group(inboundChannel.eventLoop())
                .channel(inboundChannel.getClass())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // do nothing
                    }
                })
                .option(ChannelOption.AUTO_READ, false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);

        this.doConnect(chooser.receive(inboundChannel));
    }

    private void doConnect(Backend backend) {
        ChannelFuture f = b.connect(backend.getHost(), backend.getPort());
        // this.outboundChannel = f.channel();
        f.addListener(
                (ChannelFutureListener) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        log.info("connect to backend success: {}", backend.detail());
                        Channel outboundChannel = channelFuture.channel();
                        outRef.set(outboundChannel);
                        outboundChannel.pipeline().addLast(new BackendHandler(inboundChannel, chooser, backend));
                        // read after connected
                        inboundChannel.read();
                    } else {
                        log.error("connect to backend failed: {}", channelFuture.cause().getMessage());
                        Backend next = chooser.connectFailed(inboundChannel, backend);
                        if (next != null) {
                            doConnect(next);
                        } else {
                            log.error("No available backend server");
                            ChannelUtils.closeOnFlush(inboundChannel);
                        }
                    }
                }
        );
    }

}
