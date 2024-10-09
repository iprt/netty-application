package io.intellij.netty.tcpfrp.client.service;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.exchange.both.DataPacket;
import io.intellij.netty.tcpfrp.exchange.c2s.ServiceBreakConn;
import io.intellij.netty.tcpfrp.exchange.c2s.ServiceDataPacket;
import io.intellij.netty.tcpfrp.exchange.codec.ExProtocolUtils;
import io.intellij.netty.tcpfrp.exchange.codec.ExchangeType;
import io.intellij.netty.utils.ByteUtils;
import io.intellij.netty.utils.ChannelUtils;
import io.intellij.netty.utils.CtxUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.intellij.netty.tcpfrp.exchange.SystemConfig.DATA_PACKET_USE_JSON;

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
        final Channel serviceChannel = ctx.channel();
        String serviceChannelId = CtxUtils.getChannelId(ctx);
        // how to get user channel id
        if (DATA_PACKET_USE_JSON) {
            byte[] packet = new byte[msg.readableBytes()];
            msg.readBytes(packet);
            exchangeChannel.writeAndFlush(
                    ExProtocolUtils.createProtocolData(
                            ExchangeType.C2S_SERVICE_DATA_PACKET,
                            DataPacket.builder()
                                    .from(ServiceDataPacket.class.getName())
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
        } else {
            byte[] userChannelIdBytes = this.userChannelId.getBytes();
            byte[] serviceChannelIdBytes = serviceChannelId.getBytes();

            byte[] prepareBytes = new byte[1 + 4 + userChannelIdBytes.length + serviceChannelIdBytes.length];

            int bodyLen = userChannelIdBytes.length + serviceChannelIdBytes.length + msg.readableBytes();
            byte[] bodyLenBytes = ByteUtils.getIntBytes(bodyLen);

            prepareBytes[0] = (byte) ExchangeType.C2S_SERVICE_DATA_PACKET.getType();
            System.arraycopy(bodyLenBytes, 0, prepareBytes, 1, bodyLenBytes.length);
            System.arraycopy(userChannelIdBytes, 0, prepareBytes, 1 + 4, userChannelIdBytes.length);
            System.arraycopy(serviceChannelIdBytes, 0, prepareBytes, 1 + 4 + userChannelIdBytes.length, serviceChannelIdBytes.length);

            exchangeChannel.write(Unpooled.copiedBuffer(prepareBytes));

            // 引用计数器+1 给exchangeChannel使用,节约内存
            ReferenceCountUtil.retain(msg);
            exchangeChannel.write(msg)
                    .addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            if (serviceChannel.isActive()) {
                                serviceChannel.read();
                            }
                        } else {
                            closeServiceChannel(serviceChannelId, "ServiceHandler.channelRead0");
                        }
                    });
            exchangeChannel.flush();
        }
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        String serviceChannelId = CtxUtils.getChannelId(ctx);
        exchangeChannel.writeAndFlush(
                ExProtocolUtils.createProtocolData(
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
