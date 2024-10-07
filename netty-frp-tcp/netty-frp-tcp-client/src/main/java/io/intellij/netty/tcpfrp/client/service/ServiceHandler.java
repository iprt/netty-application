package io.intellij.netty.tcpfrp.client.service;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.exchange.ExchangeProtocolUtils;
import io.intellij.netty.tcpfrp.exchange.ExchangeType;
import io.intellij.netty.tcpfrp.exchange.clientsend.GetServiceData;
import io.intellij.netty.tcpfrp.exchange.clientsend.ServiceBreakConn;
import io.intellij.netty.utils.CtxUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
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
    public static final Map<String, Channel> serviceChannelMap = new ConcurrentHashMap<>();

    private final ListeningConfig listeningConfig;
    private final String userChannelId;

    private final Channel exchangeChannel;

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        String serviceChannelId = CtxUtils.getChannelId(ctx);
        log.info("Service channelActive.Put it To Map|User={}|Service={}", this.userChannelId, serviceChannelId);
        serviceChannelMap.put(serviceChannelId, ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        log.info("获取到service的数据{}", listeningConfig);
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);

        String serviceChannelId = CtxUtils.getChannelId(ctx);
        // how to get user channel id
        exchangeChannel.writeAndFlush(
                ExchangeProtocolUtils.jsonProtocol(
                        ExchangeType.CLIENT_TO_SERVER_GET_SERVICE_DATA,
                        GetServiceData.builder()
                                .userChannelId(this.userChannelId)
                                .serviceChannelId(serviceChannelId)
                                .data(bytes)
                                .build()
                )
        );

    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        String serviceChannelId = CtxUtils.getChannelId(ctx);
        exchangeChannel.writeAndFlush(
                ExchangeProtocolUtils.jsonProtocol(
                        ExchangeType.CLIENT_TO_SERVER_LOST_REAL_SERVER_CONN,
                        ServiceBreakConn.builder()
                                .listeningConfig(listeningConfig)
                                .userChannelId(this.userChannelId)
                                .serviceChannelId(serviceChannelId)
                                .build()
                )
        );
        closeServiceChannel(serviceChannelId, "ServiceHandler.channelInactive()");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("{}|{}", CtxUtils.getRemoteAddress(ctx), cause.getMessage());
    }


    public static void closeServiceChannel(String serviceChannelId, String desc) {
        if (null == serviceChannelId) {
            return;
        }
        Channel channel = serviceChannelMap.get(serviceChannelId);
        if (channel != null) {
            log.info("CloseServiceChannel|ServiceChannelId={}|desc={}", serviceChannelId, desc);
            if (channel.isActive()) {
                channel.close();
            }
            serviceChannelMap.remove(serviceChannelId);
        }
    }

    public static void dispatch(String serviceChannelId, byte[] bytes) {
        Channel serviceChannel = serviceChannelMap.get(serviceChannelId);
        if (serviceChannel != null && serviceChannel.isActive()) {
            log.info("dispatch to service|{}", serviceChannelId);
            serviceChannel.writeAndFlush(Unpooled.copiedBuffer(bytes));
        }
    }

}
