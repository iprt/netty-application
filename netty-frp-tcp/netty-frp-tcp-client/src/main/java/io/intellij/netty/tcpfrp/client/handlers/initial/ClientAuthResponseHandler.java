package io.intellij.netty.tcpfrp.client.handlers.initial;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.protocol.client.ListeningRequest;
import io.intellij.netty.tcpfrp.protocol.server.AuthResponse;
import io.intellij.netty.utils.ChannelUtils;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * ClientAuthResponseHandler
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@RequiredArgsConstructor
@Slf4j
public class ClientAuthResponseHandler extends SimpleChannelInboundHandler<AuthResponse> {
    private final Map<String, ListeningConfig> configMap;

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        // must
        ctx.read();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, @NotNull AuthResponse authResponse) throws Exception {
        if (authResponse.isSuccess()) {
            log.info("authenticate success");
            List<Integer> listeningPorts = configMap.values().stream().map(ListeningConfig::getRemotePort).toList();
            log.info("send listening request, ports: {}", listeningPorts);
            ctx.writeAndFlush(ListeningRequest.create(listeningPorts))
                    .addListener((ChannelFutureListener) cf -> {
                        if (cf.isSuccess()) {
                            ctx.pipeline().remove(ClientAuthResponseHandler.class);
                            ctx.pipeline().addLast(new ListeningResponseHandler(configMap));

                            // for UserConnStateHandler
                            ctx.pipeline().fireChannelActive();
                        } else {
                            ChannelUtils.closeOnFlush(ctx.channel());
                        }
                    });
        } else {
            // 认证失败，服务端会主动关闭连接
            log.info("authenticate failed");
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("ClientAuthResponseHandler channelInactive");
    }

}
