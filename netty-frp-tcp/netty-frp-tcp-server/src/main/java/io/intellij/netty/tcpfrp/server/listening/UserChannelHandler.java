package io.intellij.netty.tcpfrp.server.listening;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.exchange.FrpChannel;
import io.intellij.netty.tcpfrp.protocol.DataPacket;
import io.intellij.netty.tcpfrp.protocol.server.UserConnState;
import io.intellij.netty.tcpfrp.server.handlers.UserChannelManager;
import io.intellij.netty.utils.CtxUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static io.intellij.netty.tcpfrp.server.handlers.UserChannelManager.SERVER_ID_KEY;

/**
 * UserChannelHandler
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
@Slf4j
public class UserChannelHandler extends ChannelInboundHandlerAdapter {
    private final Map<Integer, ListeningConfig> portToServer;
    private final FrpChannel frpChannel;

    /**
     * 用户连接成功
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // e.g. user ---> frp-server:3306
        final String userId = CtxUtils.getChannelId(ctx);
        UserChannelManager.getInstance().addChannel(userId, ctx.channel());

        ListeningConfig listeningConfig = portToServer.get(CtxUtils.getLocalAddress(ctx).getPort());
        log.info("用户建立了连接 |userId={}|listeningConfig={}", userId, listeningConfig);

        // 通知 frp-client，用户连接成功 但是userChannel不read数据 通过waitUserChannelRead
        frpChannel.writeAndFlush(UserConnState.accept(userId, listeningConfig))
                .addListener((ChannelFutureListener) f -> {
                    if (f.isSuccess()) {
                        // 读取数据
                        f.channel().read();
                    }
                });
    }

    /**
     * 用户发送数据
     * <p>
     * after {@link UserChannelManager#setAttrThenChannelRead(String, AttributeKey, Object)}
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf byteBuf) {
            String userId = CtxUtils.getChannelId(ctx);
            String serviceId = UserChannelManager.getInstance().getAttrValue(userId, SERVER_ID_KEY);
            log.info("接收到用户的数据 |userId={}|len={}", userId, byteBuf.readableBytes());
            frpChannel.writeAndFlush(DataPacket.create(userId, serviceId, byteBuf)).addListeners(
                    (ChannelFutureListener) f -> {
                        if (f.isSuccess()) {
                            ctx.read();
                        }
                    },
                    (ChannelFutureListener) f -> {
                        if (f.isSuccess()) {
                            f.channel().read();
                        }
                    }

            );

            return;
        }
        log.warn("unknown msg type|{}", msg.getClass());
        throw new RuntimeException("unknown msg type");
    }

    /**
     * 用户断开连接
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("用户断开了连接 |listeningConfig={}", portToServer.get(CtxUtils.getLocalAddress(ctx).getPort()));
        String userId = CtxUtils.getChannelId(ctx);
        // String serviceId = ctx.channel().attr(SERVER_ID_KEY).get();
        String serviceId = UserChannelManager.getInstance().getAttrValue(userId, SERVER_ID_KEY);
        frpChannel.writeAndFlush(UserConnState.broken(userId, serviceId))
                .addListener((ChannelFutureListener) f -> {
                    if (f.isSuccess()) {
                        // frp channel read
                        f.channel().read();
                    }
                });
    }

    @Override
    public void exceptionCaught(@NotNull ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("user channel exception caught|{}", cause.getMessage(), cause);
        ctx.close();
    }

    public static @Nullable ChannelFuture dispatch(@NotNull DataPacket dataPacket) {
        Channel channel = UserChannelManager.getInstance().getChannel(dataPacket.getUserId());
        if (channel != null && channel.isActive()) {
            return channel.writeAndFlush(dataPacket.getPacket());
        } else {
            log.warn("UserChannelHandler dispatch error| userId: {}, channel: {}, isActive: {}", dataPacket.getUserId(), channel, channel != null && channel.isActive());
            return null;
        }
    }

}
