package io.intellij.netty.tcpfrp.server.handlers.dispatch;

import io.intellij.netty.tcpfrp.commons.DispatchManager;
import io.intellij.netty.tcpfrp.commons.Listeners;
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
        log.info("[channelActive]: ReceiveServiceStateHandler");
        FrpChannel.get(ctx.channel()).read();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, @NotNull ServiceState connState) throws Exception {
        FrpChannel frpChannel = FrpChannel.get(ctx.channel());

        ConnState serviceState = ConnState.getByName(connState.getStateName());
        if (serviceState == null) {
            throw new IllegalArgumentException("channelRead0 unknown state : {}" + connState.getStateName());
        }
        switch (serviceState) {
            case SUCCESS:
                // frp-client ---> service 连接成功
                // 可以获取到 dispatchId
                frpChannel.writeAndFlush(UserState.ready(connState.getDispatchId()),
                        Listeners.read(DispatchManager.getInstance().getChannel(connState.getDispatchId())),
                        Listeners.read(frpChannel)
                );

                break;
            case FAILURE:
                // frp-client ---> service 连接断开
                frpChannel.writeAndFlushEmpty()
                        .addListeners(Listeners.releaseDispatchChannel(connState.getDispatchId(), FAILURE.getDesc()),
                                Listeners.read(frpChannel));
                break;
            case BROKEN:
                // service ---> frp-client 连接断开
                frpChannel.writeAndFlushEmpty()
                        .addListeners(Listeners.releaseDispatchChannel(connState.getDispatchId(), BROKEN.getDesc()),
                                Listeners.read(frpChannel));
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
        MultiPortNettyServer.stop(ctx.channel());
        FrpChannel.get(ctx.channel()).close();
    }

}
