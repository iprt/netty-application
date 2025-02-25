package io.intellij.netty.tcp.lb;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * EchoResponseHandler
 *
 * @author tech@intellij.io
 * @since 2025-02-25
 */
@RequiredArgsConstructor
public class EchoResponseHandler extends ChannelInboundHandlerAdapter {
    private final int port;
    private final Lock lock;
    private final Condition condition;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf byteBuf) {
            int i = byteBuf.readableBytes();
            byte[] bytes = new byte[i];
            byteBuf.readBytes(bytes);

            String response = new String(bytes);
            ChannelFuture future = ctx.writeAndFlush(response + "-" + port);
            if ("shutdown".equals(response)) {
                future.addListener(ChannelFutureListener.CLOSE);
                lock.lock();
                try {
                    condition.signalAll();
                } finally {
                    lock.unlock();
                }
            }
        }
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Channel inactive: " + ctx.channel().remoteAddress());
    }

}
