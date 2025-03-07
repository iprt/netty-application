package io.intellij.netty.tcpfrp.client.service;

import io.intellij.netty.tcpfrp.commons.DispatchManager;
import io.intellij.netty.tcpfrp.protocol.channel.DispatchPacket;
import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.intellij.netty.tcpfrp.protocol.client.ServiceState;
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
    private final String serviceName;
    private final String dispatchId;
    private final FrpChannel frpChannel;

    /**
     * 服务连接成功
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("建立服务端连接 |dispatchId={}|serviceName={}", dispatchId, serviceName);
        DispatchManager.getInstance().addChannel(dispatchId, ctx.channel());
        // BootStrap set AUTO_READ=false
        // 等待frp-server 发送 UserConnState(READY)
    }

    /**
     * 读取到服务的数据
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf byteBuf) {
            log.info("接收到服务端的数据 |dispatchId={}|serviceName={}|len={}", dispatchId, serviceName, byteBuf.readableBytes());
            frpChannel.writeAndFlush(DispatchPacket.create(dispatchId, byteBuf),
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
        log.warn("丢失服务端连接 |dispatchId={}|serviceName{}", dispatchId, serviceName);
        // frp-client -x-> mysql:3306
        frpChannel.writeAndFlush(ServiceState.connBroken(dispatchId),
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
        log.error("exception caught|dispatchId={}", dispatchId, cause);
        ctx.close();
    }

}
