package io.intellij.netty.server.socks.handlers;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.SocksPortUnificationServerHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.jetbrains.annotations.NotNull;

/**
 * SocksServerInit
 *
 * @author tech@intellij.io
 */
public class SocksServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(@NotNull SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new LoggingHandler(LogLevel.DEBUG))
                .addLast(new SocksPortUnificationServerHandler())
                .addLast(SocksServerHandler.INSTANCE);
    }

}
