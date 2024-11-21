package io.intellij.netty.example.dispatch.handlers.server;

import io.intellij.netty.example.dispatch.codec.DispatchDecoder;
import io.intellij.netty.example.dispatch.codec.encoders.DataBodyEncoder;
import io.intellij.netty.example.dispatch.codec.encoders.HeartBeatEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.jetbrains.annotations.NotNull;

/**
 * ServerInitializer
 *
 * @author tech@intellij.io
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(@NotNull SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new DispatchDecoder());
        pipeline.addLast(new HeartBeatEncoder());
        pipeline.addLast(new DataBodyEncoder());


        pipeline.addLast(new ServerHeartBeatHandler());
        pipeline.addLast(new ServerDataBodyHandler());

        pipeline.addLast(new LoginHandler());
        pipeline.addLast(new LogoutHandler());
    }
}
