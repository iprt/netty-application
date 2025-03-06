package io.intellij.netty.tcpfrp.server.handlers.dispatch;

import io.intellij.netty.tcpfrp.protocol.DataPacket;
import io.intellij.netty.tcpfrp.server.listening.UserChannelHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * ServerDispatchHandler
 * <p>
 * 接收到 frp-client 和 frp-server 封装的数据包，并分发
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@Slf4j
public class ServerDispatchHandler extends SimpleChannelInboundHandler<DataPacket> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("ServerDispatchHandler channelActive");
        ctx.read();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataPacket msg) throws Exception {
        // after UserChannel read0
        String userId = msg.getUserId();
        String serviceId = msg.getServiceId();
        log.info("ServerDispatchHandler channelRead0 userId {} serviceId {}", userId, serviceId);
        ChannelFuture dispatch = UserChannelHandler.dispatch(msg);

        if (dispatch != null) {
            dispatch.addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    Channel useChannel = f.channel();
                    useChannel.read();
                    // for ServerDispatchHandler read again
                    ctx.channel().read();
                }
            });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("ServerDispatchHandler exceptionCaught", cause);
        ctx.close();
    }

}
