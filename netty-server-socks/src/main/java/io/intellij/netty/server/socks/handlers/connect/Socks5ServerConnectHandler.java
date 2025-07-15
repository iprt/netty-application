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
import io.netty.handler.codec.socksx.v4.Socks4CommandStatus;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import static io.intellij.netty.utils.ChannelUtils.closeOnFlush;

/**
 * Socks5ServerConnectHandler
 *
 * @author tech@intellij.io
 */
@Slf4j
public class Socks5ServerConnectHandler extends SimpleChannelInboundHandler<Socks5CommandRequest> {

    private final Bootstrap b = new Bootstrap();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Socks5CommandRequest request) throws Exception {
        final Channel inboundChannel = ctx.channel();
        Promise<Channel> promise = ctx.executor().newPromise();
        promise.addListener((FutureListener<Channel>) future -> {
            final Channel outboundChannel = future.getNow();
            if (future.isSuccess()) {
                ChannelFuture responseFuture = ctx.channel().writeAndFlush(new DefaultSocks5CommandResponse(
                        Socks5CommandStatus.SUCCESS, request.dstAddrType(), request.dstAddr(), request.dstPort())
                );
                responseFuture.addListener((ChannelFutureListener) channelFuture -> {
                    ctx.pipeline().remove(Socks5ServerConnectHandler.this);
                    outboundChannel.pipeline().addLast(new RelayHandler(inboundChannel));
                    ctx.pipeline().addLast(new RelayHandler(outboundChannel));
                });
            } else {
                ctx.channel().writeAndFlush(new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, request.dstAddrType()));
                closeOnFlush(inboundChannel);
            }
        });

        b.group(inboundChannel.eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new DirectClientHandler(promise));

        b.connect(request.dstAddr(), request.dstPort()).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("connect to {}:{} success", request.dstAddr(), request.dstPort());
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
