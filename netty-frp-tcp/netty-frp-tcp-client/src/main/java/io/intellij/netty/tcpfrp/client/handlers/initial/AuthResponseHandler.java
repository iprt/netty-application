package io.intellij.netty.tcpfrp.client.handlers.initial;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.intellij.netty.tcpfrp.protocol.client.ListeningRequest;
import io.intellij.netty.tcpfrp.protocol.server.AuthResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static io.intellij.netty.tcpfrp.protocol.channel.FrpChannel.FRP_CHANNEL_KEY;

/**
 * AuthResponseHandler
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@RequiredArgsConstructor
@Slf4j
public class AuthResponseHandler extends SimpleChannelInboundHandler<AuthResponse> {
    private final Map<String, ListeningConfig> configMap;

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        FrpChannel frpChannel = ctx.channel().attr(FRP_CHANNEL_KEY).get();
        // must
        frpChannel.read();
    }

    @Override
    protected void channelRead0(@NotNull ChannelHandlerContext ctx, @NotNull AuthResponse authResponse) throws Exception {
        FrpChannel frpChannel = ctx.channel().attr(FRP_CHANNEL_KEY).get();
        if (authResponse.isSuccess()) {
            log.info("authenticate success");
            List<Integer> listeningPorts = configMap.values().stream().map(ListeningConfig::getRemotePort).toList();
            log.info("send listening request, ports: {}", listeningPorts);
            frpChannel.writeAndFlush(ListeningRequest.create(listeningPorts), channelFuture -> {
                        if (channelFuture.isSuccess()) {
                            ctx.pipeline().remove(AuthResponseHandler.class);
                            ctx.pipeline().addLast(new ListeningResponseHandler(configMap));

                            // for UserConnStateHandler
                            ctx.pipeline().fireChannelActive();
                        }
                    }
            );
        } else {
            // 认证失败，服务端会主动关闭连接
            log.info("authenticate failed");
            frpChannel.close();
        }
    }

}
