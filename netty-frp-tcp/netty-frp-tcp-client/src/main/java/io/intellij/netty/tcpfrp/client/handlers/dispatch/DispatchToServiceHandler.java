package io.intellij.netty.tcpfrp.client.handlers.dispatch;

import io.intellij.netty.tcpfrp.client.service.ServiceChannelHandler;
import io.intellij.netty.tcpfrp.protocol.DataPacket;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
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
public class DispatchToServiceHandler extends SimpleChannelInboundHandler<DataPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, @NotNull DataPacket msg) throws Exception {
        // 获取到数据包，e.g. user --- frp-server:3306 的数据包
        ChannelFuture dispatch = ServiceChannelHandler.dispatch(msg.getServiceId(), msg.getPacket());
        if (dispatch != null) {
            dispatch.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    // 读取下一个数据包
                    ctx.channel().read();
                    future.channel().read();
                } else {
                    log.error("ClientDispatchHandler channelRead0 dispatch failed", future.cause());
                }
            });
        } else {
            log.error("ClientDispatchHandler channelRead0 dispatch is null");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("ClientDispatchHandler exceptionCaught", cause);
        ctx.close();
    }

}
