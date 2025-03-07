package io.intellij.netty.tcpfrp.server.handlers.dispatch;

import io.intellij.netty.tcpfrp.protocol.ConnState;
import io.intellij.netty.tcpfrp.protocol.client.ServiceConnState;
import io.intellij.netty.tcpfrp.server.handlers.UserChannelManager;
import io.intellij.netty.tcpfrp.server.handlers.initial.ListeningRequestHandler;
import io.intellij.netty.tcpfrp.server.listening.MultiPortNettyServer;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import static io.intellij.netty.tcpfrp.server.handlers.FrpServerInitializer.MULTI_PORT_NETTY_SERVER_KEY;
import static io.intellij.netty.tcpfrp.server.handlers.UserChannelManager.SERVER_ID_KEY;

/**
 * ServiceConnStateHandler
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@Slf4j
public class ServiceConnStateHandler extends SimpleChannelInboundHandler<ServiceConnState> {

    /**
     * Triggered from {@link ListeningRequestHandler}
     */
    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        log.info("1. server conn state handler channelActive");
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
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListeners(
                        (ChannelFutureListener) channelFuture -> {
                            if (channelFuture.isSuccess()) {
                                String userId = msg.getUserId();
                                String serviceId = msg.getServiceId();
                                log.info("ServiceConnStateHandler channelRead0 userId {} serviceId {}", userId, serviceId);
                                // UserChannelHandler.notifyUserChannelRead(userId, serviceId);

                                UserChannelManager.getInstance().setAttrThenChannelRead(userId, SERVER_ID_KEY, serviceId);
                            }
                        },
                        (ChannelFutureListener) channelFuture -> {
                            if (channelFuture.isSuccess()) {
                                channelFuture.channel().read();
                            }
                        }
                );
                break;
            case FAILURE:
                // frp-client ---> service 连接失败 只有userId 没有serviceId
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListeners(
                        (ChannelFutureListener) channelFuture -> {
                            if (channelFuture.isSuccess()) {
                                UserChannelManager.getInstance().release(msg.getUserId());
                            }
                        },
                        (ChannelFutureListener) channelFuture -> {
                            if (channelFuture.isSuccess()) {
                                ctx.read();
                            }
                        }
                );
                break;
            case BROKEN:
                // frp-client ---> service 连接断开 只有userId 没有serviceId
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListeners(
                        (ChannelFutureListener) channelFuture -> {
                            if (channelFuture.isSuccess()) {
                                UserChannelManager.getInstance().release(msg.getUserId());
                            }
                        },
                        (ChannelFutureListener) channelFuture -> {
                            if (channelFuture.isSuccess()) {
                                ctx.read();
                            }
                        }
                );
                break;
            default:
                log.error("ServiceConnStateHandler channelRead0 unknown state {}", serviceState);
                break;
        }

    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        log.warn("与 frp-client 断开连接，释放所有 userChannel, 关闭监听服务");
        UserChannelManager.getInstance().releaseAll();

        MultiPortNettyServer multiPortNettyServer = ctx.channel().attr(MULTI_PORT_NETTY_SERVER_KEY).get();
        if (multiPortNettyServer != null) {
            multiPortNettyServer.stop();
        }
        ctx.close();
    }

}
