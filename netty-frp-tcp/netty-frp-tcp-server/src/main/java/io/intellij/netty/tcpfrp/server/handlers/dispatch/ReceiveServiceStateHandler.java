package io.intellij.netty.tcpfrp.server.handlers.dispatch;

import io.intellij.netty.tcpfrp.commons.DispatchManager;
import io.intellij.netty.tcpfrp.protocol.ConnState;
import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.intellij.netty.tcpfrp.protocol.client.ServiceState;
import io.intellij.netty.tcpfrp.protocol.server.UserState;
import io.intellij.netty.tcpfrp.server.handlers.initial.ListeningRequestHandler;
import io.intellij.netty.tcpfrp.server.listening.MultiPortNettyServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import static io.intellij.netty.tcpfrp.protocol.ConnState.BROKEN;
import static io.intellij.netty.tcpfrp.protocol.ConnState.FAILURE;
import static io.intellij.netty.tcpfrp.protocol.channel.FrpChannel.FRP_CHANNEL_KEY;
import static io.intellij.netty.tcpfrp.server.handlers.initial.ListeningRequestHandler.MULTI_PORT_NETTY_SERVER_KEY;

/**
 * ReceiveServiceStateHandler
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@Slf4j
public class ReceiveServiceStateHandler extends SimpleChannelInboundHandler<ServiceState> {
    /**
     * Triggered from {@link ListeningRequestHandler}
     */
    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        log.info("channelActive: server conn state handler");
        FrpChannel frpChannel = FrpChannel.build(ctx.channel());
        ctx.channel().attr(FRP_CHANNEL_KEY).set(frpChannel);
        frpChannel.read();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, @NotNull ServiceState connState) throws Exception {
        FrpChannel frpChannel = ctx.channel().attr(FRP_CHANNEL_KEY).get();

        ConnState serviceState = ConnState.getByName(connState.getStateName());
        if (serviceState == null) {
            throw new IllegalArgumentException("ServiceConnStateHandler channelRead0 unknown state " + connState.getStateName());
        }
        switch (serviceState) {
            case SUCCESS:
                // frp-client ---> service 连接成功 可以获取到 dispatchId
                frpChannel.writeAndFlush(UserState.ready(connState.getDispatchId()),
                        f -> {
                            if (f.isSuccess()) {
                                DispatchManager.getInstance().initiativeChannelRead(connState.getDispatchId());
                            }
                        },
                        f -> {
                            if (f.isSuccess()) {
                                frpChannel.read();
                            }
                        }
                );
                break;
            case FAILURE:
                // frp-client ---> service 连接断开
                frpChannel.writeAndFlush(
                        f -> {
                            if (f.isSuccess()) {
                                DispatchManager.getInstance().release(connState.getDispatchId(), FAILURE.getDesc());
                            }
                        },
                        f -> {
                            if (f.isSuccess()) {
                                frpChannel.read();
                            }
                        }
                );
                break;
            case BROKEN:
                // frp-client ---> service 连接断开
                frpChannel.writeAndFlush(
                        f -> {
                            if (f.isSuccess()) {
                                DispatchManager.getInstance().release(connState.getDispatchId(), BROKEN.getDesc());
                            }
                        },
                        f -> {
                            if (f.isSuccess()) {
                                frpChannel.read();
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
        log.warn("与 frp-client 断开连接, 释放所有 userChannel, 关闭监听服务");
        DispatchManager.getInstance().releaseAll();

        MultiPortNettyServer multiPortNettyServer = ctx.channel().attr(MULTI_PORT_NETTY_SERVER_KEY).get();
        if (multiPortNettyServer != null) {
            multiPortNettyServer.stop();
        }

        FrpChannel frpChannel = ctx.channel().attr(FRP_CHANNEL_KEY).get();
        frpChannel.close();
    }

}
