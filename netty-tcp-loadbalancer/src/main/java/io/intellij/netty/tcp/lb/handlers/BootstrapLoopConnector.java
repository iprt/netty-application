package io.intellij.netty.tcp.lb.handlers;

import io.intellij.netty.tcp.lb.config.Backend;
import io.intellij.netty.tcp.lb.selector.BackendSelector;
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

import static io.intellij.netty.tcp.lb.handlers.FrontendInboundHandler.OUTBOUND_CHANNEL_KEY;

/**
 * BootstrapLoopConnector
 *
 * @author tech@intellij.io
 * @since 2025-02-24
 */
@RequiredArgsConstructor
@Slf4j
public class BootstrapLoopConnector {
    private final Bootstrap b = new Bootstrap();
    private final BackendSelector selector;
    private final Channel inboundChannel;

    public void connect() {
        b.group(inboundChannel.eventLoop())
                .channel(inboundChannel.getClass())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // do nothing, just for a pipeline
                        // 避免 BackendOutboundHandler 需要 @Sharable
                    }
                })
                .option(ChannelOption.AUTO_READ, false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);

        this.loopConnect(selector.select());
    }

    private void loopConnect(Backend backend) {
        ChannelFuture f = b.connect(backend.getHost(), backend.getPort());
        f.addListener(
                (ChannelFutureListener) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        log.info("connect to backend success: {}", backend.detail());
                        Channel outboundChannel = channelFuture.channel();
                        inboundChannel.attr(OUTBOUND_CHANNEL_KEY).set(outboundChannel);

                        outboundChannel.pipeline().addLast(new BackendOutboundHandler(inboundChannel, selector, backend));
                        // read after connected
                        inboundChannel.read();
                    } else {
                        log.error("connect to backend failed: {}", channelFuture.cause().getMessage());
                        Backend next = selector.nextIfConnectFailed(backend);
                        if (next != null) {
                            loopConnect(next);
                        } else {
                            log.error("No available backend server");
                            ChannelUtils.closeOnFlush(inboundChannel);
                        }
                    }
                }
        );
    }

}
