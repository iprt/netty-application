package io.intellij.netty.tcpfrp.server.handlers.initial;

import io.intellij.netty.tcpfrp.commons.DispatchManager;
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
        FrpChannel.get(ctx.channel()).read();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Ping ping) throws Exception {
        log.info("HeatBeat PING|{}", ping);
        FrpChannel frpChannel = FrpChannel.get(ctx.channel());
        frpChannel.write(Pong.create(ping.getName()));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        FrpChannel.get(ctx.channel()).flush().read();
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        log.warn("release dispatch channel");
        DispatchManager.getInstance().releaseAll();
        super.channelInactive(ctx);

        log.warn("close frp channel");
        FrpChannel.get(ctx.channel()).close();
    }

}
