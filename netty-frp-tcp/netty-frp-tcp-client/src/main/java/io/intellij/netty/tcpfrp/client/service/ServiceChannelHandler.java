package io.intellij.netty.tcpfrp.client.service;

import io.intellij.netty.tcpfrp.client.handlers.ServiceChannelManager;
import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.protocol.DataPacket;
import io.intellij.netty.tcpfrp.protocol.client.ServiceConnState;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
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
    // private static final Map<String, Channel> serviceId2Channel = new ConcurrentHashMap<>();

    private final ListeningConfig listeningConfig;
    private final String userId;
    // serviceId 等价与当前 Handler 的 channelId
    private final String serviceId;
    // frp-client --- frp-server
    private final Channel frpsChannel;

    /**
     * 服务连接成功
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("建立服务端连接 |config={}", listeningConfig);
        ServiceChannelManager.getInstance().addChannel(serviceId, ctx.channel());
        // BootStrap AutoRead=false
        ctx.read();
    }

    /**
     * 读取到服务的数据
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf byteBuf) {
            log.info("接收到服务端的数据 |serviceId={}|config={}|len={}", serviceId, listeningConfig, byteBuf.readableBytes());
            frpsChannel.writeAndFlush(DataPacket.create(userId, serviceId, byteBuf)).addListeners(
                    (ChannelFutureListener) f -> {
                        if (f.isSuccess()) {
                            f.channel().read();
                        }
                    },
                    (ChannelFutureListener) channelFuture -> {
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

    public static ChannelFuture dispatch(String serviceId, ByteBuf packet) {
        Channel channel = ServiceChannelManager.getInstance().getChannel(serviceId);
        if (channel != null && channel.isActive()) {
            return channel.writeAndFlush(packet);
        } else {
            // 可能是服务关闭了连接
            log.error("ServiceHandler dispatch error| serviceId: {}, channel: {}, isActive: {}", serviceId, channel, channel != null && channel.isActive());
            return null;
        }
    }

    /**
     * 服务连接断开
     * <p>
     * 1. 通知 frp-server，服务连接断开
     * 2. 关闭服务的 channel
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("丢失服务端连接 |serviceId={}|config={}", serviceId, listeningConfig);
        // frp-client -x-> mysql:3306
        if (frpsChannel.isActive()) {
            frpsChannel.writeAndFlush(ServiceConnState.connBroken(userId, serviceId)).addListener(
                    (ChannelFutureListener) f -> {
                        if (f.isSuccess()) {
                            f.channel().read();
                        }
                    }
            );
        }
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("ServiceHandler exceptionCaught, userId={}, serviceId={}", userId, serviceId, cause);
        ctx.close();
    }

    // public static void close(String serviceId) {
    //     Channel serviceChannel = serviceId2Channel.remove(serviceId);
    //     ChannelUtils.close(serviceChannel);
    // }

    // public static void closeAll() {
    //     serviceId2Channel.values().forEach(ChannelUtils::close);
    //     serviceId2Channel.clear();
    // }

}
