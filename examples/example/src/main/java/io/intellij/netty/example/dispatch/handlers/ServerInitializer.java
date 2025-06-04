package io.intellij.netty.example.dispatch.handlers;

import io.intellij.netty.example.dispatch.codec.DispatchDecoder;
import io.intellij.netty.example.dispatch.codec.encoders.DataBodyEncoder;
import io.intellij.netty.example.dispatch.codec.encoders.HeartBeatEncoder;
import io.intellij.netty.example.dispatch.handlers.server.LoginHandler;
import io.intellij.netty.example.dispatch.handlers.server.LogoutHandler;
import io.intellij.netty.example.dispatch.handlers.server.ServerDataBodyHandler;
import io.intellij.netty.example.dispatch.handlers.server.ServerHeartBeatHandler;
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
        ChannelPipeline p = ch.pipeline();
        p.addLast(new ByteCountingHandler());

        p.addLast(new DispatchDecoder());
        p.addLast(new HeartBeatEncoder());
        p.addLast(new DataBodyEncoder());


        p.addLast(new ServerHeartBeatHandler());
        p.addLast(new ServerDataBodyHandler());

        p.addLast(new LoginHandler());
        p.addLast(new LogoutHandler());
    }
}
