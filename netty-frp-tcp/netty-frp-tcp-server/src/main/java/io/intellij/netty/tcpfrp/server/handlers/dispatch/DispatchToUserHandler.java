package io.intellij.netty.tcpfrp.server.handlers.dispatch;

import io.intellij.netty.tcpfrp.commons.DispatchManager;
import io.intellij.netty.tcpfrp.commons.Listeners;
import io.intellij.netty.tcpfrp.protocol.channel.DispatchPacket;
import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * DispatchToUserHandler
 * <p>
 * 接收到 frp-client 和 frp-server 封装的数据包，并分发
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@Slf4j
public class DispatchToUserHandler extends SimpleChannelInboundHandler<DispatchPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DispatchPacket msg) throws Exception {
        // after UserChannel read0
        DispatchManager.getInstance().dispatch(msg,
                Listeners.read(),
                Listeners.read(FrpChannel.get(ctx.channel()))
        );
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("channelInactive: dispatch to user handler");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("ServerDispatchHandler exceptionCaught", cause);
        ctx.close();
    }

}
