package io.intellij.netty.tcpfrp.server.listening;

import io.intellij.netty.tcpfrp.commons.DispatchManager;
import io.intellij.netty.tcpfrp.commons.Listeners;
import io.intellij.netty.tcpfrp.protocol.channel.DispatchIdUtils;
import io.intellij.netty.tcpfrp.protocol.channel.DispatchPacket;
import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.intellij.netty.tcpfrp.protocol.server.UserState;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * UserChannelHandler
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
@Slf4j
public class UserChannelHandler extends ChannelInboundHandlerAdapter {
    private final int listeningPort;
    private final FrpChannel frpChannel;

    /**
     * 用户连接成功
     */
    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        // e.g. user ---> frp-server:3306
        final String dispatchId = DispatchIdUtils.getDispatchId(ctx.channel());

        DispatchManager.getInstance().addChannel(dispatchId, ctx.channel());

        log.info("[USER] 用户建立了连接 |dispatchId={}|port={}", dispatchId, this.listeningPort);

        // 等待frp-client 发送 ServiceConnState(SUCCESS),然后READ
        // AUTO_READ = false
        frpChannel.writeAndFlush(UserState.accept(dispatchId, this.listeningPort))
                .addListeners(Listeners.read(frpChannel));

    }

    /**
     * 用户发送数据
     * <p>
     * after {@link Listeners#read(Channel)}
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf byteBuf) {
            String dispatchId = DispatchIdUtils.getDispatchId(ctx.channel());
            log.debug("接收到用户的数据 |dispatchId={}|port={}|len={}", dispatchId, this.listeningPort, byteBuf.readableBytes());
            frpChannel.writeAndFlush(DispatchPacket.create(dispatchId, byteBuf),
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
        log.warn("[USER] 用户断开了连接 |dispatchId={}", dispatchId);
        frpChannel.writeAndFlush(UserState.broken(dispatchId), Listeners.read(frpChannel));
    }

    @Override
    public void exceptionCaught(@NotNull ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exception caught|{}", cause.getMessage(), cause);
        ctx.close();
    }

}
