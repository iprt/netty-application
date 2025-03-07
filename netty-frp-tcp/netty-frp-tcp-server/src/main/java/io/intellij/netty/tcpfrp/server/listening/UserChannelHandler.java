package io.intellij.netty.tcpfrp.server.listening;

import io.intellij.netty.tcpfrp.commons.DispatchChannelManager;
import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.protocol.channel.DataPacket;
import io.intellij.netty.tcpfrp.protocol.channel.DispatchIdUtils;
import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.intellij.netty.tcpfrp.protocol.server.UserConnState;
import io.intellij.netty.utils.CtxUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

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
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        // e.g. user ---> frp-server:3306
        final String dispatchId = DispatchIdUtils.getDispatchId(ctx.channel());

        DispatchChannelManager.getInstance().addChannel(dispatchId, ctx.channel());

        ListeningConfig listeningConfig = portToServer.get(CtxUtils.getLocalAddress(ctx).getPort());
        ctx.channel().attr(LISTENING_CONFIG_KEY).set(listeningConfig);

        log.info("用户建立了连接 |dispatchId={}|name={}", dispatchId, listeningConfig.getName());

        // 通知 frp-client，用户连接成功 但是userChannel不read数据, 在 setAttrThenChannelRead 之后 read
        frpChannel.writeAndFlush(UserConnState.accept(dispatchId, listeningConfig),
                f -> {
                    if (f.isSuccess()) {
                        frpChannel.read();
                    }
                });
    }

    /**
     * 用户发送数据
     * <p>
     * after {@link UserChannelManager#initiativeChannelRead(String)}
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf byteBuf) {
            String dispatchId = DispatchIdUtils.getDispatchId(ctx.channel());
            log.info("接收到用户的数据 |dispatchId={}|name={}|len={}", dispatchId, ctx.channel().attr(LISTENING_CONFIG_KEY).get().getName(), byteBuf.readableBytes());

            frpChannel.writeAndFlush(DataPacket.create(dispatchId, byteBuf),
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
        String dispatchId = DispatchIdUtils.getDispatchId(ctx.channel());
        log.warn("用户断开了连接 |dispatchId={}|name={}", dispatchId, ctx.channel().attr(LISTENING_CONFIG_KEY).get().getName());
        frpChannel.writeAndFlush(UserConnState.broken(dispatchId),
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
