package io.intellij.netty.tcpfrp.server.listening;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.protocol.DataPacket;
import io.intellij.netty.tcpfrp.protocol.server.UserConnState;
import io.intellij.netty.utils.CtxUtils;
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
 * UserChannelHandler
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
@Slf4j
public class UserChannelHandler extends ChannelInboundHandlerAdapter {
    private static final Map<String, Channel> userChannelMap = new ConcurrentHashMap<>();
    private static final Map<String, String> userId2ServiceId = new ConcurrentHashMap<>();

    private final Map<Integer, ListeningConfig> portToServer;
    private final Channel frpcChannel;

    // auto_read = false
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 接收到用户的连接，发送给frp-client
        // e.g. user ---> frp-server:3306
        final String userId = CtxUtils.getChannelId(ctx);
        ListeningConfig listeningConfig = portToServer.get(CtxUtils.getLocalAddress(ctx).getPort());

        if (frpcChannel.isActive()) {
            // 通知 frp-client，用户连接成功
            frpcChannel.writeAndFlush(UserConnState.accept(userId, listeningConfig))
                    .addListener((ChannelFutureListener) cf -> {
                        if (cf.isSuccess()) {
                            ctx.read();
                            userChannelMap.put(userId, ctx.channel());
                        }
                    });
        }

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("user channel read msg");
        // after notifyUserChannelRead
        if (msg instanceof ByteBuf byteBuf) {
            final String userId = CtxUtils.getChannelId(ctx);
            // 读取到用户的数据
            if (frpcChannel.isActive()) {
                frpcChannel.writeAndFlush(DataPacket.create(userId, userId2ServiceId.get(userId), byteBuf))
                        .addListener((ChannelFutureListener) f -> {
                            if (f.isSuccess()) {
                                // auto_read = false
                                f.channel().read();
                                // auto_read = false
                                frpcChannel.read();
                            }
                        });
            }

        } else {
            log.warn("unknown msg type|{}", msg.getClass());
            throw new RuntimeException("unknown msg type");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // e.g. user -x-> frp-server:3306
        final String userId = CtxUtils.getChannelId(ctx);
        // 用户断开连接
        log.info("user channel inactive|{}", ctx.channel().remoteAddress());
        if (frpcChannel.isActive()) {
            frpcChannel.writeAndFlush(UserConnState.broken(userId, userId2ServiceId.get(userId)))
                    .addListener((ChannelFutureListener) f -> {
                        if (f.isSuccess()) {
                            // this is frp-client channel
                            f.channel().read();
                        }
                    });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exception caught|{}", cause.getMessage(), cause);
        ctx.close();
    }

    // AUTO_READ = false
    public static boolean notifyUserChannelRead(String userId, String serviceId) {
        userId2ServiceId.put(userId, serviceId);
        Channel channel = userChannelMap.get(userId);
        if (channel != null) {
            if (channel.isActive()) {
                channel.read();
                return true;
            }
        }
        return false;
    }

    public static ChannelFuture dispatch(DataPacket dataPacket) {
        Channel channel = userChannelMap.get(dataPacket.getUserId());
        if (channel != null && channel.isActive()) {
            return channel.writeAndFlush(dataPacket.getPacket());
        }
        return null;
    }

    public static void close(String userId) {
        Channel channel = userChannelMap.get(userId);
        if (channel != null && channel.isActive()) {
            channel.close();
        }
        userChannelMap.remove(userId);
        userId2ServiceId.remove(userId);
    }

    public static void closeAll() {
        for (Map.Entry<String, Channel> entry : userChannelMap.entrySet()) {
            Channel userChannel = entry.getValue();
            if (userChannel != null && userChannel.isActive()) {
                userChannel.close();
            }
        }
        userChannelMap.clear();
        userId2ServiceId.clear();
    }

}
