package io.intellij.netty.example.dispatch.handlers.client;

import io.intellij.netty.example.dispatch.codec.DispatchDecoder;
import io.intellij.netty.example.dispatch.codec.encoders.DataBodyEncoder;
import io.intellij.netty.example.dispatch.codec.encoders.HeartBeatEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.jetbrains.annotations.NotNull;

/**
 * ClientInitializer
 *
 * @author tech@intellij.io
 */
public class ClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(@NotNull SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new DispatchDecoder());
        pipeline.addLast(new HeartBeatEncoder());
        pipeline.addLast(new DataBodyEncoder());

        pipeline.addLast(new ClientHeartBeatHandler());
        pipeline.addLast(new ClientDataBodyHandler());

        pipeline.addLast(new ResponseLogHandler());
    }
}
