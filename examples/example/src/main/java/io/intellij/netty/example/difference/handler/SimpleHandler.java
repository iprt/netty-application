package io.intellij.netty.example.difference.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * SimpleHandler
 * <p>
 * SimpleChannelInboundHandler 的 ByteBuf 引用计数器会主动 -1 (默认的)
 *
 * @author tech@intellij.io
 * @since 2025-02-27
 */
@Slf4j
public class SimpleHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("channel active");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        int i = msg.readableBytes();
        byte[] bytes = new byte[i];
        msg.readBytes(bytes);
        log.info("receive msg: {}", new String(bytes));

        log.info("当前的 ByteBuf 引用计数: {}", msg.refCnt());

        ReferenceCountUtil.retain(msg);
        msg.writeBytes(bytes);
        ctx.write(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("channel inactive");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exception caught", cause);
    }
}
