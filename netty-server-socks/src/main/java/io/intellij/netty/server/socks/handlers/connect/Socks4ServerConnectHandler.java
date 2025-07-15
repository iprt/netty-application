package io.intellij.netty.server.socks.handlers.connect;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v4.DefaultSocks4CommandResponse;
import io.netty.handler.codec.socksx.v4.Socks4CommandRequest;
import io.netty.handler.codec.socksx.v4.Socks4CommandStatus;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import static io.intellij.netty.utils.ChannelUtils.closeOnFlush;

/**
 * Socks4ServerConnectHandler
 *
 * @author tech@intellij.io
 */
@Slf4j
public class Socks4ServerConnectHandler extends SimpleChannelInboundHandler<Socks4CommandRequest> {

    private final Bootstrap b = new Bootstrap();

    @Override
    protected void channelRead0(@NotNull ChannelHandlerContext ctx, @NotNull Socks4CommandRequest request) throws Exception {
        final Channel inboundChannel = ctx.channel();
        Promise<Channel> promise = ctx.executor().newPromise();
        promise.addListener((FutureListener<Channel>) future -> {
            final Channel outbountChannel = future.getNow();
            if (future.isSuccess()) {
                ChannelFuture responseFuture = ctx.channel().writeAndFlush(
                        new DefaultSocks4CommandResponse(Socks4CommandStatus.SUCCESS));
                responseFuture.addListener((ChannelFutureListener) channelFuture -> {
                    ctx.pipeline().remove(Socks4ServerConnectHandler.this);
                    outbountChannel.pipeline().addLast(new RelayHandler(inboundChannel));
                    ctx.pipeline().addLast(new RelayHandler(outbountChannel));
                });
            } else {
                ctx.channel().writeAndFlush(new DefaultSocks4CommandResponse(Socks4CommandStatus.REJECTED_OR_FAILED));
                closeOnFlush(ctx.channel());
            }
        });

        String dstAddr = request.dstAddr();
        int dstPort = request.dstPort();
        b.group(inboundChannel.eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new DirectClientHandler(promise));

        b.connect(dstAddr, dstPort).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("connect to {}:{} success", dstAddr, dstPort);
            } else {
                // Close the connection if the connection attempt has failed.
                log.error("connect to {}:{} failed", request.dstAddr(), request.dstPort());
                ctx.channel().writeAndFlush(
                        new DefaultSocks4CommandResponse(Socks4CommandStatus.REJECTED_OR_FAILED)
                );
                closeOnFlush(inboundChannel);
            }
        });

    }
}
