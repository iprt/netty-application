package io.intellij.netty.tcpfrp.client.handlers.initial;

import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.intellij.netty.tcpfrp.protocol.client.ListeningConfig;
import io.intellij.netty.tcpfrp.protocol.client.ListeningRequest;
import io.intellij.netty.tcpfrp.protocol.server.AuthResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

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
    protected void channelRead0(@NotNull ChannelHandlerContext ctx, @NotNull AuthResponse authResponse) throws Exception {
        FrpChannel frpChannel = FrpChannel.getBy(ctx.channel());
        if (authResponse.isSuccess()) {
            log.info("authenticate success");
            List<Integer> listeningPorts = configMap.values().stream().map(ListeningConfig::getRemotePort).toList();
            log.info("send listening request, ports: {}", listeningPorts);
            frpChannel.write(ListeningRequest.create(listeningPorts), channelFuture -> {
                        if (channelFuture.isSuccess()) {
                            ChannelPipeline p = ctx.pipeline();
                            p.remove(this);
                            p.addLast(new ListeningResponseHandler(configMap));
                            // ReceiveUserStateHandler
                            p.fireChannelActive();
                        }
                    }
            );
        } else {
            // 认证失败，服务端会主动关闭连接
            log.warn("authenticate failed");
            frpChannel.close();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        FrpChannel.getBy(ctx.channel()).flush();
    }
}
