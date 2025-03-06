package io.intellij.netty.tcpfrp.server.handlers.initial;

import io.intellij.netty.tcpfrp.protocol.client.AuthRequest;
import io.intellij.netty.tcpfrp.protocol.server.AuthResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ServerAuthHandler
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@RequiredArgsConstructor
@Slf4j
public class ServerAuthHandler extends ChannelInboundHandlerAdapter {
    private final String configToken;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof AuthRequest authRequest) {
            if (authenticate(authRequest)) {
                ctx.writeAndFlush(AuthResponse.success())
                        .addListener((ChannelFutureListener) cf -> {
                            if (cf.isSuccess()) {
                                ChannelPipeline pipeline = cf.channel().pipeline();
                                pipeline.remove(ServerAuthHandler.class);
                                pipeline.addLast(new ListeningRequestHandler());
                                pipeline.fireChannelActive();
                            } else {
                                ctx.close();
                            }
                        });
            } else {
                ctx.writeAndFlush(AuthResponse.failure())
                        .addListener(ChannelFutureListener.CLOSE);
            }
        } else {
            // 第一个消息不是认证消息，关闭连接
            ctx.close();
        }
    }

    private boolean authenticate(AuthRequest authRequest) {
        boolean authResult = authRequest.getToken().equals(configToken);
        if (authResult) {
            log.info("authenticate success");
        } else {
            log.error("authenticate failed");
        }
        return authResult;
    }

}
