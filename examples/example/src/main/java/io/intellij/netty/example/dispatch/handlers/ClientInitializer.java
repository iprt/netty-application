package io.intellij.netty.example.dispatch.handlers;

import io.intellij.netty.example.dispatch.codec.DispatchDecoder;
import io.intellij.netty.example.dispatch.codec.encoders.DataBodyEncoder;
import io.intellij.netty.example.dispatch.codec.encoders.HeartBeatEncoder;
import io.intellij.netty.example.dispatch.codec.encoders.LoginReqEncoder;
import io.intellij.netty.example.dispatch.handlers.client.ClientDataBodyHandler;
import io.intellij.netty.example.dispatch.handlers.client.ClientHeartBeatHandler;
import io.intellij.netty.example.dispatch.handlers.client.ResponseLogHandler;
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
        ChannelPipeline p = ch.pipeline();

        p.addLast(new DispatchDecoder());
        p.addLast(new HeartBeatEncoder());
        p.addLast(new DataBodyEncoder());

        p.addLast(new LoginReqEncoder());

        p.addLast(new ClientHeartBeatHandler());
        p.addLast(new ClientDataBodyHandler());

        p.addLast(new ResponseLogHandler());
    }
}
