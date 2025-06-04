package io.intellij.netty.example.dispatch.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * ByteCountingHandler
 *
 * @author tech@intellij.io
 */
@Slf4j
public class ByteCountingHandler extends ChannelInboundHandlerAdapter {

    private long totalBytesReceived = 0;

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        if (msg instanceof ByteBuf byteBuf) {
            totalBytesReceived += byteBuf.readableBytes();
        }

        // 调用下一个处理器
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        // 打印总的接收到的字节数
        log.info("Total bytes received: {}", totalBytesReceived);
        super.channelInactive(ctx);
    }
}
