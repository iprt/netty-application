package io.intellij.netty.tcpfrp.server.handlers;

import io.intellij.netty.tcpfrp.config.ServerConfig;
import io.intellij.netty.tcpfrp.protocol.FrpDecoder;
import io.intellij.netty.tcpfrp.protocol.FrpEncoder;
import io.intellij.netty.tcpfrp.server.handlers.initial.AuthRequestHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * FrpServerInitializer
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@RequiredArgsConstructor
public class FrpServerInitializer extends ChannelInitializer<SocketChannel> {
    private final ServerConfig config;

    @Override
    protected void initChannel(@NotNull SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        if (config.isEnableSSL()) {
            pipeline.addLast(config.getSslContext().newHandler(ch.alloc()));
        }

        pipeline.addLast(FrpDecoder.serverDecoder())
                .addLast(FrpEncoder.basicMsgEncoder())
                .addLast(FrpEncoder.dispatchEncoder());


        pipeline.addLast(new AuthRequestHandler(config.getAuthToken()));
    }

}
