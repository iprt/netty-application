package io.intellij.netty.tcpfrp.client.handlers.dispatch;

import io.intellij.netty.tcpfrp.commons.DispatchManager;
import io.intellij.netty.tcpfrp.protocol.channel.DispatchPacket;
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
public class DispatchToServiceHandler extends SimpleChannelInboundHandler<DispatchPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, @NotNull DispatchPacket msg) throws Exception {
        // 获取到数据包，e.g. user --- frp-server:3306 的数据包
        ChannelFuture dispatch = DispatchManager.getInstance().dispatch(msg);
        if (dispatch != null) {
            dispatch.addListeners(
                    (ChannelFutureListener) f -> {
                        if (f.isSuccess()) {
                            // 读取下一个数据包
                            ctx.channel().read();
                        } else {
                            log.error("ClientDispatchHandler channelRead0 dispatch failed", f.cause());
                        }
                    },
                    (ChannelFutureListener) f -> {
                        if (f.isSuccess()) {
                            // 读取下一个数据包
                            f.channel().read();
                        } else {
                            log.error("ClientDispatchHandler channelRead0 dispatch failed", f.cause());
                        }
                    }
            );
        } else {
            log.error("ClientDispatchHandler channelRead0 dispatch is null");
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("ClientDispatchHandler exceptionCaught", cause);
        ctx.close();
    }

}
