package io.intellij.netty.tcpfrp.client.handlers.initial;

import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.intellij.netty.tcpfrp.protocol.heartbeat.Ping;
import io.intellij.netty.tcpfrp.protocol.heartbeat.Pong;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * PongHandler
 *
 * @author tech@intellij.io
 * @since 2025-03-10
 */
@Slf4j
public class PongHandler extends SimpleChannelInboundHandler<Pong> {

    /**
     * Triggered from {@link ListeningResponseHandler}
     */
    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        FrpChannel frpChannel = FrpChannel.get(ctx.channel());

        ctx.executor().scheduleAtFixedRate(() -> frpChannel.writeAndFlush(Ping.create("frp-client")),
                1, 5, TimeUnit.SECONDS);

        log.info("[channelActive]: Pong Handler");

        // must but just once
        frpChannel.read();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Pong msg) throws Exception {
        log.info("HeatBeat PONG|{}", msg);
        FrpChannel.get(ctx.channel()).read();
    }


    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        log.warn("close frp channel");
        FrpChannel.get(ctx.channel()).close();

        super.channelInactive(ctx);
    }
}
