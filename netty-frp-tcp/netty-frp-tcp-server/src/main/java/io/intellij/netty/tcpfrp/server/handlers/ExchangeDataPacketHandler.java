package io.intellij.netty.tcpfrp.server.handlers;

import io.intellij.netty.tcpfrp.exchange.codec.ExchangeProtocolDataPacket;
import io.intellij.netty.tcpfrp.exchange.codec.ExchangeType;
import io.intellij.netty.tcpfrp.server.listening.UserHandler;
import io.intellij.netty.utils.CtxUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import static io.intellij.netty.tcpfrp.exchange.codec.ExchangeType.C2S_SERVICE_DATA_PACKET;

/**
 * ExchangeDataPacketHandler
 *
 * @author tech@intellij.io
 */
@Slf4j
public class ExchangeDataPacketHandler extends SimpleChannelInboundHandler<ExchangeProtocolDataPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ExchangeProtocolDataPacket dataPacket) throws Exception {
        ExchangeType exchangeType = dataPacket.exchangeType();
        if (C2S_SERVICE_DATA_PACKET == exchangeType) {
            UserHandler.dispatch(dataPacket);
        } else {
            log.error("error exchange type|{}", exchangeType);
            ctx.close();
        }
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("localAddress={}|remoteAddress={}", CtxUtils.getLocalAddress(ctx), CtxUtils.getRemoteAddress(ctx), cause);
    }

}
