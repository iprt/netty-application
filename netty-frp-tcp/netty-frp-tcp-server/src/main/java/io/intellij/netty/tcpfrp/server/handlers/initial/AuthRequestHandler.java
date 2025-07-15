package io.intellij.netty.tcpfrp.server.handlers.initial;

import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.intellij.netty.tcpfrp.protocol.client.AuthRequest;
import io.intellij.netty.tcpfrp.protocol.server.AuthResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * AuthRequestHandler
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@RequiredArgsConstructor
@Slf4j
public class AuthRequestHandler extends ChannelInboundHandlerAdapter {
    private final String configToken;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FrpChannel frpChannel = FrpChannel.getBy(ctx.channel());
        if (msg instanceof AuthRequest authRequest) {
            if (authenticate(authRequest)) {
                frpChannel.write(AuthResponse.success(),
                        channelFuture -> {
                            if (channelFuture.isSuccess()) {
                                ChannelPipeline p = ctx.pipeline();
                                p.addLast(new ListeningRequestHandler());
                                p.remove(this);
                                p.fireChannelActive();
                            } else {
                                frpChannel.close();
                            }
                        });
            } else {
                frpChannel.write(AuthResponse.failure(), ChannelFutureListener.CLOSE);
            }
        } else {
            // 第一个消息不是认证消息，关闭连接
            frpChannel.close();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        FrpChannel.getBy(ctx.channel()).flush();
    }

    private boolean authenticate(@NotNull AuthRequest authRequest) {
        boolean authResult = authRequest.getToken().equals(configToken);
        if (authResult) {
            log.info("authenticate client success");
        } else {
            log.error("authenticate client failed");
        }
        return authResult;
    }

}
