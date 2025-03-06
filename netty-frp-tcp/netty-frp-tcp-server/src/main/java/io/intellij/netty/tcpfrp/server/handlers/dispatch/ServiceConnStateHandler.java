package io.intellij.netty.tcpfrp.server.handlers.dispatch;

import io.intellij.netty.tcpfrp.protocol.ConnState;
import io.intellij.netty.tcpfrp.protocol.client.ServiceConnState;
import io.intellij.netty.tcpfrp.server.listening.MultiPortNettyServer;
import io.intellij.netty.tcpfrp.server.listening.UserChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import static io.intellij.netty.tcpfrp.server.handlers.FrpServerInitializer.MULTI_PORT_NETTY_SERVER_KEY;

/**
 * ServiceConnStateHandler
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@Slf4j
public class ServiceConnStateHandler extends SimpleChannelInboundHandler<ServiceConnState> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("ServiceConnStateHandler channelActive");
        ctx.read();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, @NotNull ServiceConnState msg) throws Exception {
        ConnState serviceState = ConnState.getByName(msg.getConnState());
        if (serviceState == null) {
            throw new IllegalArgumentException("ServiceConnStateHandler channelRead0 unknown state " + msg.getConnState());
        }
        switch (serviceState) {
            case SUCCESS:
                // frp-client ---> service 连接成功 可以获取到 serviceId
                String userId = msg.getUserId();
                String serviceId = msg.getServiceId();
                log.info("ServiceConnStateHandler channelRead0 userId {} serviceId {}", userId, serviceId);
                if (UserChannelHandler.notifyUserChannelRead(userId, serviceId)) {
                    // for ServerDispatchHandler read again
                    ctx.pipeline().fireChannelActive();
                }
                break;
            case FAILURE:
                // frp-client ---> service 连接失败 只有userId 没有serviceId
                UserChannelHandler.close(msg.getUserId());
                break;
            case BROKEN:
                // frp-client ---> service 连接断开 只有userId 没有serviceId
                UserChannelHandler.close(msg.getUserId());
                break;
            default:
                log.error("ServiceConnStateHandler channelRead0 unknown state {}", serviceState);
                break;
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("ServiceConnStateHandler channelInactive");
        MultiPortNettyServer multiPortNettyServer = ctx.channel().attr(MULTI_PORT_NETTY_SERVER_KEY).get();
        if (multiPortNettyServer != null) {
            multiPortNettyServer.stop();
        }

        UserChannelHandler.closeAll();

        ctx.close();
    }

}
