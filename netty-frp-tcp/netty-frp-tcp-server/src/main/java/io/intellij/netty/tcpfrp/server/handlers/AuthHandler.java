package io.intellij.netty.tcpfrp.server.handlers;

import io.intellij.netty.tcpfrp.exchange.SysConfig;
import io.intellij.netty.tcpfrp.exchange.codec.ExchangeDecoder;
import io.intellij.netty.tcpfrp.exchange.codec.ExchangeEncoder;
import io.intellij.netty.utils.CtxUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AuthHandler
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
@Slf4j
public class AuthHandler extends SimpleChannelInboundHandler<String> {
    static final String HANDLER_NAME = AuthHandler.class.getName();
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

            boolean dataPacketUseJson = SysConfig.DATA_PACKET_USE_JSON;

            ctx.pipeline().addLast("ExchangeDecoder", new ExchangeDecoder(dataPacketUseJson));
            ctx.pipeline().addLast("ExchangeEncoder", new ExchangeEncoder());

            ctx.pipeline().addLast(new ExchangeHandler(dataPacketUseJson));

            if (!dataPacketUseJson) {
                ctx.pipeline().addLast(new ExchangeDataPacketHandler());
            }

        } else {
            ctx.close();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("{}|{}", CtxUtils.getRemoteAddress(ctx), cause.getMessage());
    }
}
