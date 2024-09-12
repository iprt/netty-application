package io.intellij.netty.server.tcpproxy.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import org.jetbrains.annotations.NotNull;

/**
 * HexDumpProxyFrontendHandler
 *
 * @author tech@intellij.io
 */
public class HexDumpProxyFrontendHandler extends ChannelInboundHandlerAdapter {

    private final String remoteHost;
    private final int remotePort;

    // As we use inboundChannel.eventLoop() when building the Bootstrap this does not need to be volatile as
    // the outboundChannel will use the same EventLoop (and therefore Thread) as the inboundChannel.
    // 这个channel是连接远程服务器的channel
    private Channel outboundChannel;

    public HexDumpProxyFrontendHandler(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final Channel inboundChannel = ctx.channel();

        // Start the connection attempt.
        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop())
                .channel(ctx.channel().getClass())
                // 切入点1
                .handler(new HexDumpProxyBackendHandler(inboundChannel))
                .option(ChannelOption.AUTO_READ, false);
        ChannelFuture f = b.connect(remoteHost, remotePort);
        outboundChannel = f.channel();
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(@NotNull ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    // connection complete start to read first data
                    inboundChannel.read();
                } else {
                    // Close the connection if the connection attempt has failed.
                    inboundChannel.close();
                }
            }
        });
    }


    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        // 连接 baidu.com 的channel
        if (outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(msg).addListener(
                    (ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            // was able to flush out data, start to read the next chunk
                            // 切入点
                            ctx.channel().read();
                        } else {
                            future.channel().close();
                        }
                    }
            );
        }
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    /**
     * Close on flush.
     *
     * @param ch the ch
     */
    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER)
                    .addListener(ChannelFutureListener.CLOSE);
        }

    }

}
