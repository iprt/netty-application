package io.intellij.netty.tcpfrp.client.handlers.dispatch;

import io.intellij.netty.tcpfrp.commons.Listeners;
import io.intellij.netty.tcpfrp.protocol.channel.DispatchManager;
import io.intellij.netty.tcpfrp.protocol.channel.DispatchPacket;
import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * DispatchToServiceHandler
 * <p>
 * after ListeningResponseHandler
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@Slf4j
public class DispatchToServiceHandler extends SimpleChannelInboundHandler<DispatchPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, @NotNull DispatchPacket msg) throws Exception {
        // 获取到数据包，e.g. user --- frp-server:3306 的数据包
        DispatchManager.getBy(ctx.channel()).dispatch(msg, Listeners.read());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        FrpChannel.getBy(ctx.channel()).read();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        DispatchManager.getBy(ctx.channel()).releaseAll();
    }

}
