package io.intellij.netty.tcpfrp.client.service;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.protocol.DataPacket;
import io.intellij.netty.tcpfrp.protocol.client.ServiceConnState;
import io.intellij.netty.utils.ChannelUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ServiceChannelHandler
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
@Slf4j
public class ServiceChannelHandler extends ChannelInboundHandlerAdapter {
    private static final Map<String, Channel> serviceId2Channel = new ConcurrentHashMap<>();

    private final ListeningConfig listeningConfig;
    private final String userId;

    // serviceId 等价与当前 Handler 的 channelId
    private final String serviceId;
    // frp-client --- frp-server
    private final Channel frpsChannel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("ServiceHandler channelActive, userId={}, serviceId={} | listeningConfig={}", userId, serviceId, listeningConfig);
        serviceId2Channel.put(serviceId, ctx.channel());
        ctx.read();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("service channel receive msg");
        if (msg instanceof ByteBuf byteBuf) {
            // 直接包装并转发到 frp-server
            frpsChannel.writeAndFlush(DataPacket.create(userId, serviceId, byteBuf))
                    .addListener((ChannelFutureListener) f -> {
                        if (f.isSuccess()) {
                            f.channel().read();
                            ctx.channel().read();
                        }
                    });

        } else {
            log.error("ServiceHandler channelRead error, msg: {}", msg);
            throw new IllegalArgumentException("msg is not ByteBuf");
        }
    }

    public static ChannelFuture dispatch(String serviceId, ByteBuf packet) {
        Channel channel = serviceId2Channel.get(serviceId);
        if (channel != null) {
            return channel.writeAndFlush(packet);
        } else {
            log.error("ServiceHandler dispatch error, serviceId: {} not found", serviceId);
            return null;
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // frp-client -x-> mysql:3306
        if (frpsChannel.isActive()) {
            frpsChannel.writeAndFlush(ServiceConnState.connBroken(userId, serviceId))
                    .addListener((ChannelFutureListener) f -> {
                                if (f.isSuccess()) {
                                    f.channel().pipeline().fireChannelActive();
                                }
                            }
                    );
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("ServiceHandler exceptionCaught, userId={}, serviceId={}", userId, serviceId, cause);
        ctx.close();
    }


    public static void close(String serviceId) {
        Channel channel = serviceId2Channel.remove(serviceId);
        ChannelUtils.closeOnFlush(channel);
    }

    public static void closeAll() {
        for (Map.Entry<String, Channel> entry : serviceId2Channel.entrySet()) {
            Channel channel = entry.getValue();
            if (channel != null) {
                ChannelUtils.closeOnFlush(channel);
            }
        }
        serviceId2Channel.clear();
    }

}
