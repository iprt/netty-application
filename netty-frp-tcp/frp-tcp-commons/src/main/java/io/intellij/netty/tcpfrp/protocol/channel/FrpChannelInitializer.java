package io.intellij.netty.tcpfrp.protocol.channel;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.jetbrains.annotations.NotNull;

import static io.intellij.netty.tcpfrp.protocol.channel.FrpChannel.build;

/**
 * FrpChannelInitializer
 *
 * @author tech@intellij.io
 * @since 2025-03-11
 */
public abstract class FrpChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(@NotNull SocketChannel ch) throws Exception {
        build(ch);
        this.initChannel0(ch);
    }

    protected abstract void initChannel0(@NotNull SocketChannel ch) throws Exception;

}
