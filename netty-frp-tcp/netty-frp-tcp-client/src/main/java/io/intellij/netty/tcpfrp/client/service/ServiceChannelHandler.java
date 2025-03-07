package io.intellij.netty.tcpfrp.client.service;

import io.intellij.netty.tcpfrp.client.handlers.ServiceChannelManager;
import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.protocol.channel.DataPacket;
import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.intellij.netty.tcpfrp.protocol.client.ServiceConnState;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ServiceChannelHandler
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
@Slf4j
public class ServiceChannelHandler extends ChannelInboundHandlerAdapter {
    private final ListeningConfig listeningConfig;
    private final String userId;
    // serviceId 等价与当前 Handler 的 channelId
    private final String serviceId;

    private final FrpChannel frpChannel;


    /**
     * 服务连接成功
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("建立服务端连接 |name={}", listeningConfig.getName());
        ServiceChannelManager.getInstance().addChannel(userId, ctx.channel());
        // BootStrap AutoRead=false
        ctx.read();
    }

    /**
     * 读取到服务的数据
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf byteBuf) {
            log.info("接收到服务端的数据 |serviceId={}|name={}|len={}", serviceId, listeningConfig.getName(), byteBuf.readableBytes());
            frpChannel.writeAndFlush(DataPacket.create(userId, serviceId, byteBuf),
                    f -> {
                        if (f.isSuccess()) {
                            frpChannel.read();
                        }
                    },
                    channelFuture -> {
                        if (channelFuture.isSuccess()) {
                            ctx.read();
                        }
                    }
            );

            return;
        }
        log.error("ServiceHandler channelRead error, msg: {}", msg);
        throw new IllegalArgumentException("msg is not ByteBuf");
    }


    /**
     * 服务连接断开
     * <p>
     * 1. 通知 frp-server，服务连接断开
     * 2. 关闭服务的 channel
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("丢失服务端连接 |serviceId={}|name={}", serviceId, listeningConfig.getName());
        // frp-client -x-> mysql:3306
        frpChannel.writeAndFlush(ServiceConnState.connBroken(userId, serviceId),
                f -> {
                    if (f.isSuccess()) {
                        frpChannel.read();
                    }
                }
        );
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("ServiceHandler exceptionCaught, userId={}, serviceId={}", userId, serviceId, cause);
        ctx.close();
    }

}
