package io.intellij.netty.tcpfrp.server.listening;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.intellij.netty.tcpfrp.protocol.channel.DataPacket;
import io.intellij.netty.tcpfrp.protocol.server.UserConnState;
import io.intellij.netty.tcpfrp.server.handlers.UserChannelManager;
import io.intellij.netty.utils.CtxUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static io.intellij.netty.tcpfrp.server.handlers.UserChannelManager.SERVICE_ID_KEY;

/**
 * UserChannelHandler
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
@Slf4j
public class UserChannelHandler extends ChannelInboundHandlerAdapter {
    private static final AttributeKey<ListeningConfig> LISTENING_CONFIG_KEY = AttributeKey.valueOf("listeningConfig");

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
        ctx.channel().attr(LISTENING_CONFIG_KEY).set(listeningConfig);
        log.info("用户建立了连接 |userId={}|name={}", userId, listeningConfig.getName());

        // 通知 frp-client，用户连接成功 但是userChannel不read数据, 在 setAttrThenChannelRead 之后 read
        frpChannel.writeAndFlush(UserConnState.accept(userId, listeningConfig),
                f -> {
                    if (f.isSuccess()) {
                        frpChannel.read();
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
            log.info("接收到用户的数据 |userId={}|name={}|len={}", userId, ctx.channel().attr(LISTENING_CONFIG_KEY).get().getName(), byteBuf.readableBytes());

            String serviceId = UserChannelManager.getInstance().getAttrValue(userId, SERVICE_ID_KEY);
            frpChannel.writeAndFlush(DataPacket.create(userId, serviceId, byteBuf),
                    f -> {
                        if (f.isSuccess()) {
                            ctx.read();
                        }
                    },
                    f -> {
                        if (f.isSuccess()) {
                            frpChannel.read();
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
        log.warn("用户断开了连接 |name={}", portToServer.get(CtxUtils.getLocalAddress(ctx).getPort()).getName());
        String userId = CtxUtils.getChannelId(ctx);
        // String serviceId = ctx.channel().attr(SERVER_ID_KEY).get();
        String serviceId = UserChannelManager.getInstance().getAttrValue(userId, SERVICE_ID_KEY);
        frpChannel.writeAndFlush(UserConnState.broken(userId, serviceId),
                f -> {
                    if (f.isSuccess()) {
                        // frp channel read
                        frpChannel.read();
                    }
                });
    }

    @Override
    public void exceptionCaught(@NotNull ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("user channel exception caught|{}", cause.getMessage(), cause);
        ctx.close();
    }

}
