package io.intellij.netty.tcpfrp.protocol.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * ByteCountingHandler
 *
 * @author tech@intellij.io
 * @since 2025-03-11
 */
@Slf4j
public class ByteCountingHandler extends ChannelInboundHandlerAdapter {
    private long totalBytesReceived = 0;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // must
        ctx.read();
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf byteBuf) {
            totalBytesReceived += byteBuf.readableBytes();
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        log.info("byte counting|id={}|received={}B", DispatchIdUtils.getDispatchId(ctx.channel()), totalBytesReceived);
        super.channelInactive(ctx);
    }

}
