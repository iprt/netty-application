package io.intellij.netty.server.socks;

import io.intellij.netty.server.socks.handlers.SocksServerHandler;
import io.intellij.netty.server.socks.handlers.socks5auth.Authentication;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.SocksPortUnificationServerHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * SocksServerInit
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
public class SocksServerInitializer extends ChannelInitializer<SocketChannel> {
    private final Authentication authentication;

    @Override
    protected void initChannel(@NotNull SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new LoggingHandler(LogLevel.DEBUG));
        p.addLast(new SocksPortUnificationServerHandler());
        p.addLast(SocksServerHandler.getInstance(authentication));
    }

}
