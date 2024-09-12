package io.intellij.netty.server.tcp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.jetbrains.annotations.NotNull;

/**
 * ServerInitializer
 *
 * @author tech@intellij.io
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(@NotNull SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(new PrintHandler());
    }
}
