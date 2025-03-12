package io.intellij.netty.tcpfrp.server.handlers.initial;

import io.intellij.netty.tcpfrp.protocol.channel.DispatchManager;
import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.intellij.netty.tcpfrp.protocol.heartbeat.Ping;
import io.intellij.netty.tcpfrp.protocol.heartbeat.Pong;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * PingHandler
 * <p>
 * receive ping, and send pong
 *
 * @author tech@intellij.io
 * @since 2025-03-10
 */
@Slf4j
public class PingHandler extends SimpleChannelInboundHandler<Ping> {

    /**
     * Triggered from {@link ListeningRequestHandler}
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("[channelActive]: Ping Handler");
        DispatchManager.buildIn(ctx.channel());
        FrpChannel.getBy(ctx.channel()).read();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Ping ping) throws Exception {
        log.info("HeatBeat PING|{}", ping);
        FrpChannel frpChannel = FrpChannel.getBy(ctx.channel());
        frpChannel.write(Pong.create(ping.getName()));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        FrpChannel.getBy(ctx.channel()).flush().read();
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        log.warn("release dispatch channel");
        FrpChannel frpChannel = FrpChannel.getBy(ctx.channel());
        DispatchManager.getBy(frpChannel.getBy()).releaseAll();
        super.channelInactive(ctx);

        log.warn("close frp channel");
        frpChannel.close();
    }

}
