package io.intellij.netty.example.difference.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * AdapterHandler
 * <p>
 * ChannelInboundHandlerAdapter 的 ByteBuf 引用计数器不会主动 -1
 *
 * @author tech@intellij.io
 * @since 2025-02-27
 */
@Slf4j
public class AdapterHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("channel active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("channel inactive");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf byteBuf) {
            int i = byteBuf.readableBytes();
            byte[] bytes = new byte[i];
            byteBuf.readBytes(bytes);

            log.info("当前的 ByteBuf 引用计数: {}", byteBuf.refCnt());

            log.info("channel read: {}", new String(bytes));
            byteBuf.writeBytes(bytes);

            ctx.write(byteBuf);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exception caught: {}", cause.getMessage());
    }
}
