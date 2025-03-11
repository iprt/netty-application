package io.intellij.netty.tcpfrp.client.handlers.initial;

import io.intellij.netty.tcpfrp.client.handlers.dispatch.DispatchToServiceHandler;
import io.intellij.netty.tcpfrp.client.handlers.dispatch.ReceiveUserStateHandler;
import io.intellij.netty.tcpfrp.protocol.client.ListeningConfig;
import io.intellij.netty.tcpfrp.protocol.server.ListeningResponse;
import io.intellij.netty.utils.ChannelUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * ListeningResponseHandler
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@RequiredArgsConstructor
@Slf4j
public class ListeningResponseHandler extends SimpleChannelInboundHandler<ListeningResponse> {
    private final Map<String, ListeningConfig> configMap;

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, @NotNull ListeningResponse listeningResponse) throws Exception {
        if (listeningResponse.isSuccess()) {
            log.info("listening request success");
            ctx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER)
                    .addListener((ChannelFutureListener) channelFuture -> {
                        if (channelFuture.isSuccess()) {
                            ChannelPipeline p = ctx.pipeline();
                            p.remove(this);
                            p.addLast(new PongHandler())
                                    .addLast(new ReceiveUserStateHandler(configMap))
                                    .addLast(new DispatchToServiceHandler());
                            p.fireChannelActive();
                        } else {
                            ChannelUtils.closeOnFlush(ctx.channel());
                        }
                    });
        } else {
            Map<Integer, Boolean> listeningStatus = listeningResponse.getListeningStatus();
            log.warn("listening request failure|{}", listeningStatus);
            ctx.close();
        }
    }

}
