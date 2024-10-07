package io.intellij.netty.client.reconnect;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * ClientInitializer
 *
 * @author tech@intellij.io
 */
public class ClientInitializer extends ChannelInitializer<SocketChannel> {

    private final int UUID_LEN = UUID.randomUUID().toString().length();

    @Override
    protected void initChannel(@NotNull SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new FixedLengthFrameDecoder(UUID_LEN));
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());
        pipeline.addLast(new ClientHandler());
    }
}
