package io.intellij.netty.tcpfrp.client.handlers;

import io.intellij.netty.tcpfrp.client.handlers.initial.AuthResponseHandler;
import io.intellij.netty.tcpfrp.config.ClientConfig;
import io.intellij.netty.tcpfrp.protocol.channel.FrpChannelInitializer;
import io.intellij.netty.tcpfrp.protocol.codec.decoder.FrpDecoder;
import io.intellij.netty.tcpfrp.protocol.codec.encoder.FrpEncoder;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * FrpClientInitializer
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@RequiredArgsConstructor
public class FrpClientInitializer extends FrpChannelInitializer {
    private final ClientConfig clientConfig;

    @Override
    protected void initChannel0(@NotNull SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        if (clientConfig.isEnableSSL()) {
            p.addLast(clientConfig.getSslContext().newHandler(ch.alloc()));
        }

        p.addLast(FrpDecoder.clientDecoder())
                .addLast(FrpEncoder.basicMsgEncoder())
                .addLast(FrpEncoder.dispatchEncoder());

        p.addLast(new AuthResponseHandler(clientConfig.getListeningConfigMap()));

    }

}
