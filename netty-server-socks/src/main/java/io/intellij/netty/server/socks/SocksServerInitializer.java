package io.intellij.netty.server.socks;

import io.intellij.netty.server.socks.handlers.SocksServerHandler;
import io.intellij.netty.server.socks.handlers.socks5auth.Authenticator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.SocksPortUnificationServerHandler;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * SocksServerInitializer
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
public class SocksServerInitializer extends ChannelInitializer<SocketChannel> {
    private final Authenticator authenticator;

    @Override
    protected void initChannel(@NotNull SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new SocksPortUnificationServerHandler());
        p.addLast(SocksServerHandler.getInstance(authenticator));
    }

}
