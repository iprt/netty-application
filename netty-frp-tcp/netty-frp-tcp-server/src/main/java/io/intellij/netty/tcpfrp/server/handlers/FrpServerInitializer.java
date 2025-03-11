package io.intellij.netty.tcpfrp.server.handlers;

import io.intellij.netty.tcpfrp.config.ServerConfig;
import io.intellij.netty.tcpfrp.protocol.channel.FrpChannelInitializer;
import io.intellij.netty.tcpfrp.protocol.codec.FrpCodec;
import io.intellij.netty.tcpfrp.server.handlers.initial.AuthRequestHandler;
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
public class FrpServerInitializer extends FrpChannelInitializer {
    private final ServerConfig config;

    @Override
    protected void initChannel0(@NotNull SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        if (config.isEnableSSL()) {
            pipeline.addLast(config.getSslContext().newHandler(ch.alloc()));
        }

        pipeline.addLast(FrpCodec.serverDecoder())
                .addLast(FrpCodec.basicMsgEncoder())
                .addLast(FrpCodec.dispatchEncoder());


        pipeline.addLast(new AuthRequestHandler(config.getAuthToken()));
    }

}
