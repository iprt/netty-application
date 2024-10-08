package io.intellij.netty.tcpfrp.client.handlers;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.exchange.ExProtocolUtils;
import io.intellij.netty.tcpfrp.exchange.ExchangeType;
import io.intellij.netty.tcpfrp.exchange.c2s.ListeningConfigReport;
import io.intellij.netty.tcpfrp.exchange.codec.ExchangeDecoder;
import io.intellij.netty.tcpfrp.exchange.codec.ExchangeEncoder;
import io.intellij.netty.utils.CtxUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * AuthHandler
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
@Slf4j
public class AuthHandler extends SimpleChannelInboundHandler<String> {
    public static final String HANDLER_NAME = AuthHandler.class.getName();

    private final String token;

    private final Map<String, ListeningConfig> listeningConfigMap;

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isActive()) {
            ctx.writeAndFlush(token);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String token) throws Exception {
        log.info("receive echo from server|token={}", token);
        if (!this.token.equals(token)) {
            log.error("");
            ctx.close();
        } else {
            log.info("client token validate success");
            ctx.pipeline().remove(HANDLER_NAME);
            ctx.pipeline().remove("FixedLengthFrameDecoder");
            ctx.pipeline().remove("StringDecoder");
            ctx.pipeline().remove("StringEncoder");

            ctx.pipeline().addLast(new ExchangeDecoder());
            ctx.pipeline().addLast(new ExchangeEncoder());
            ctx.pipeline().addLast(new ExchangeHandler());

            if (ctx.channel().isActive()) {
                ctx.channel().writeAndFlush(
                        ExProtocolUtils.jsonProtocol(
                                ExchangeType.C2S_SEND_CONFIG,
                                ListeningConfigReport.builder().listeningConfigMap(listeningConfigMap).build()
                        )
                );
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("{}|{}", CtxUtils.getRemoteAddress(ctx), cause.getMessage());
    }
}
