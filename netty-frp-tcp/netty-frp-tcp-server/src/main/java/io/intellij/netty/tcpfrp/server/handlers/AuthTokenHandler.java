package io.intellij.netty.tcpfrp.server.handlers;

import io.intellij.netty.tcpfrp.exchange.codec.ExchangeDecoder;
import io.intellij.netty.tcpfrp.exchange.codec.ExchangeEncoder;
import io.intellij.netty.utils.CtxUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AuthTokenHandler
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
@Slf4j
public class AuthTokenHandler extends SimpleChannelInboundHandler<String> {
    public static final String HANDLER_NAME = AuthTokenHandler.class.getName();

    private final String token;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String token) throws Exception {

        if (this.token.equals(token)) {
            log.info("echo token to client|{}|{}", CtxUtils.getRemoteAddress(ctx), token);
            ctx.writeAndFlush(token);

            ctx.pipeline().remove(HANDLER_NAME);
            ctx.pipeline().remove("FixedLengthFrameDecoder");
            ctx.pipeline().remove("StringEncoder");
            ctx.pipeline().remove("StringDecoder");

            ctx.pipeline().addLast("ExchangeDecoder", new ExchangeDecoder());
            ctx.pipeline().addLast("ExchangeEncoder", new ExchangeEncoder());
            ctx.pipeline().addLast(new ExchangeHandler());

        } else {
            ctx.close();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("{}|{}", CtxUtils.getRemoteAddress(ctx), cause.getMessage());
    }
}
