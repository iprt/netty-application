package io.intellij.netty.tcpfrp.client.handlers.initial;

import io.intellij.netty.tcpfrp.client.handlers.dispatch.DispatchToServiceHandler;
import io.intellij.netty.tcpfrp.client.handlers.dispatch.ReceiveUserConnStateHandler;
import io.intellij.netty.tcpfrp.protocol.server.ListeningResponse;
import io.intellij.netty.utils.ChannelUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * ListeningResponseHandler
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@Slf4j
public class ListeningResponseHandler extends SimpleChannelInboundHandler<ListeningResponse> {

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, @NotNull ListeningResponse listeningResponse) throws Exception {
        if (listeningResponse.isSuccess()) {
            ctx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER)
                    .addListener((ChannelFutureListener) channelFuture -> {
                        if (channelFuture.isSuccess()) {
                            ctx.pipeline().remove(ListeningResponseHandler.class);
                            ctx.pipeline().addLast(new ReceiveUserConnStateHandler())
                                    .addLast(new DispatchToServiceHandler());
                            // for UserConnStateHandler
                            ctx.pipeline().fireChannelActive();
                        } else {
                            ChannelUtils.closeOnFlush(ctx.channel());
                        }
                    });
        } else {
            Map<Integer, Boolean> listeningStatus = listeningResponse.getListeningStatus();
            log.error("请求frp-server监听失败, 监听状态: {}", listeningStatus);
            ctx.close();
        }
    }

}
