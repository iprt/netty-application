package io.intellij.netty.tcpfrp.client.service;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.exchange.ExProtocolUtils;
import io.intellij.netty.tcpfrp.exchange.ExchangeType;
import io.intellij.netty.tcpfrp.exchange.clientsend.ServiceBreakConn;
import io.intellij.netty.tcpfrp.exchange.clientsend.ServiceDataPacket;
import io.intellij.netty.utils.ChannelUtils;
import io.intellij.netty.utils.CtxUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ServiceHandler
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
@Slf4j
public class ServiceHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Map<String, Channel> serviceChannelMap = new ConcurrentHashMap<>();

    private final ListeningConfig listeningConfig;
    private final String userChannelId;
    private final Channel exchangeChannel;

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        String serviceChannelId = CtxUtils.getChannelId(ctx);
        log.info("Service channelActive.Put it To Map|User={}|Service={}", this.userChannelId, serviceChannelId);
        serviceChannelMap.put(serviceChannelId, ctx.channel());
        // AUTO_READ=false
        ctx.read();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        byte[] packet = new byte[msg.readableBytes()];
        msg.readBytes(packet);
        final Channel serviceChannel = ctx.channel();
        String serviceChannelId = CtxUtils.getChannelId(ctx);
        // how to get user channel id
        exchangeChannel.writeAndFlush(
                ExProtocolUtils.jsonProtocol(
                        ExchangeType.C2S_SERVICE_DATA_PACKET,
                        ServiceDataPacket.builder()
                                .userChannelId(this.userChannelId).serviceChannelId(serviceChannelId)
                                .packet(packet)
                                .build()
                )
        ).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                if (serviceChannel.isActive()) {
                    serviceChannel.read();
                }
            } else {
                closeServiceChannel(serviceChannelId, "ServiceHandler.channelRead0");
            }
        });

    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        String serviceChannelId = CtxUtils.getChannelId(ctx);
        exchangeChannel.writeAndFlush(
                ExProtocolUtils.jsonProtocol(
                        ExchangeType.C2S_LOST_REAL_SERVER_CONN,
                        ServiceBreakConn.builder()
                                .listeningConfig(listeningConfig)
                                .userChannelId(this.userChannelId).serviceChannelId(serviceChannelId)
                                .build()
                )
        );
        closeServiceChannel(serviceChannelId, "ServiceHandler.channelInactive()");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("localAddress={}|remoteAddress={}", CtxUtils.getLocalAddress(ctx), CtxUtils.getRemoteAddress(ctx), cause);
        String serviceChannelId = CtxUtils.getChannelId(ctx);
        closeServiceChannel(serviceChannelId, "ServiceHandler.exceptionCaught()");
    }

    public static void closeServiceChannel(String serviceChannelId, String desc) {
        if (null == serviceChannelId) {
            return;
        }
        Channel channel = serviceChannelMap.get(serviceChannelId);
        if (channel != null) {
            log.error("CloseServiceChannel|ServiceChannelId={}|desc={}", serviceChannelId, desc);
            ChannelUtils.closeOnFlush(channel);
            serviceChannelMap.remove(serviceChannelId);
        }
    }

    public static void closeAllServiceChannels() {
        log.error("close all service channels");
        serviceChannelMap.values().forEach(channel -> {
            if (channel.isActive()) {
                channel.close();
            }
        });
    }

    public static void dispatch(String serviceChannelId, byte[] packet) {
        Channel serviceChannel = serviceChannelMap.get(serviceChannelId);
        if (serviceChannel != null && serviceChannel.isActive()) {
            log.info("dispatch to service|serviceChannelId={}", serviceChannelId);
            serviceChannel.writeAndFlush(Unpooled.copiedBuffer(packet))
                    .addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            future.channel().read();
                        } else {
                            future.channel().close();
                        }
                    });
        }
    }

}
