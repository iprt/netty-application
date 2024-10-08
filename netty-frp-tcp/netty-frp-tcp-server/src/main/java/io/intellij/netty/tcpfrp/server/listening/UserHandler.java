package io.intellij.netty.tcpfrp.server.listening;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.exchange.both.DataPacket;
import io.intellij.netty.tcpfrp.exchange.codec.ExProtocolUtils;
import io.intellij.netty.tcpfrp.exchange.codec.ExchangeType;
import io.intellij.netty.tcpfrp.exchange.s2c.UserBreakConn;
import io.intellij.netty.tcpfrp.exchange.s2c.UserCreateConn;
import io.intellij.netty.tcpfrp.exchange.s2c.UserDataPacket;
import io.intellij.netty.utils.ChannelUtils;
import io.intellij.netty.utils.ConnHostPort;
import io.intellij.netty.utils.CtxUtils;
import io.netty.buffer.ByteBuf;
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

/**
 * UserHandler
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
@Slf4j
public class UserHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Map<String, Channel> userChannelMap = new ConcurrentHashMap<>();
    private static final Map<String, String> userChannelId2ServiceChannelId = new ConcurrentHashMap<>();

    private final Map<Integer, ListeningConfig> portToServer;
    private final Channel exchangeChannel;

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        // e.g. user -sync-> localhost:3306
        String userChannelId = CtxUtils.getChannelId(ctx);
        userChannelMap.put(userChannelId, ctx.channel());

        ConnHostPort localInfo = CtxUtils.getLocalAddress(ctx);
        ListeningConfig listeningConfig = portToServer.get(localInfo.getPort());
        // user conn listening server
        if (exchangeChannel.isActive()) {
            exchangeChannel.writeAndFlush(
                    ExProtocolUtils.createProtocolData(
                            ExchangeType.S2C_RECEIVE_USER_CONN_CREATE,
                            UserCreateConn.builder()
                                    .listeningConfig(listeningConfig).userChannelId(userChannelId)
                                    .build()
                    )
            );
        }
    }

    // AUTO_READ = false
    public static void notifyUserChannelRead(String userChannelId, String serviceChannelId) {
        userChannelId2ServiceChannelId.put(userChannelId, serviceChannelId);
        Channel channel = userChannelMap.get(userChannelId);
        if (channel != null) {
            if (channel.isActive()) {
                channel.read();
            }
        }
    }

    // childOption(ChannelOption.AUTO_READ, false)
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        // e.g. user -write-> localhost:3306
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);

        Channel userChannel = ctx.channel();
        String userChannelId = CtxUtils.getChannelId(ctx);
        // take service channel id
        String serviceChannelId = userChannelId2ServiceChannelId.get(userChannelId);

        if (serviceChannelId == null) {
            ctx.close();
        } else {
            exchangeChannel.writeAndFlush(
                    ExProtocolUtils.createProtocolData(
                            ExchangeType.S2C_USER_DATA_PACKET,
                            DataPacket.builder()
                                    .from(UserDataPacket.class.getName())
                                    .userChannelId(userChannelId).serviceChannelId(serviceChannelId)
                                    .packet(bytes).build()
                    )
            ).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    if (userChannel.isActive()) {
                        userChannel.read();
                    }
                } else {
                    closeUserChannel(userChannelId, "UserHandler.channelRead0");
                }
            });
        }

    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        String userChannelId = CtxUtils.getChannelId(ctx);
        ConnHostPort localInfo = CtxUtils.getLocalAddress(ctx);

        ListeningConfig listeningConfig = portToServer.get(localInfo.getPort());

        if (exchangeChannel.isActive()) {
            exchangeChannel.writeAndFlush(
                    ExProtocolUtils.createProtocolData(ExchangeType.S2C_RECEIVE_USER_CONN_BREAK,
                            UserBreakConn.builder()
                                    .listeningConfig(listeningConfig)
                                    .userChannelId(userChannelId)
                                    .build()
                    )
            );
            closeUserChannel(userChannelId, "UserHandler.channelInactive");
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("{}|{}", CtxUtils.getRemoteAddress(ctx), cause.getMessage());
    }

    public static void closeUserChannel(String userChannelId, String desc) {
        if (userChannelId == null) {
            return;
        }
        Channel channel = userChannelMap.get(userChannelId);
        if (channel != null) {
            log.info("CloseUserChannel|UserChannelId={}|desc={}", userChannelId, desc);
            ChannelUtils.closeOnFlush(channel);
            userChannelMap.remove(userChannelId);
        }

        userChannelId2ServiceChannelId.remove(userChannelId);
    }


    public static void dispatch(String userChannelId, ByteBuf msg) {
        Channel userChannel = userChannelMap.get(userChannelId);
        if (userChannel != null && userChannel.isActive()) {
            // log.info("dispatch to user|userChannelId={}", userChannelId);
            userChannel.writeAndFlush(msg)
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
