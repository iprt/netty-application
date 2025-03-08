package io.intellij.netty.tcpfrp.client.handlers.dispatch;

import io.intellij.netty.tcpfrp.commons.DispatchManager;
import io.intellij.netty.tcpfrp.commons.Listeners;
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
        DispatchManager.getInstance().dispatch(msg,
                Listeners.read(),
                Listeners.read(FrpChannel.get(ctx.channel()))
        );
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("ClientDispatchHandler exceptionCaught", cause);
        ctx.close();
    }

}
