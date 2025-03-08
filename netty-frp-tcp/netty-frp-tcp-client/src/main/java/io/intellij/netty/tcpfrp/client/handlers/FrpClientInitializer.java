package io.intellij.netty.tcpfrp.client.handlers;

import io.intellij.netty.tcpfrp.client.handlers.initial.AuthResponseHandler;
import io.intellij.netty.tcpfrp.config.ClientConfig;
import io.intellij.netty.tcpfrp.protocol.FrpDecoder;
import io.intellij.netty.tcpfrp.protocol.FrpEncoder;
import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import static io.intellij.netty.tcpfrp.protocol.channel.FrpChannel.FRP_CHANNEL_KEY;

/**
 * FrpClientInitializer
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@RequiredArgsConstructor
public class FrpClientInitializer extends ChannelInitializer<SocketChannel> {
    private final ClientConfig clientConfig;

    @Override
    protected void initChannel(@NotNull SocketChannel ch) throws Exception {
        FrpChannel frpChannel = FrpChannel.build(ch);
        ch.attr(FRP_CHANNEL_KEY).set(frpChannel);

        ChannelPipeline pipeline = ch.pipeline();
        if (clientConfig.isEnableSSL()) {
            pipeline.addLast(clientConfig.getSslContext().newHandler(ch.alloc()));
        }

        pipeline.addLast(FrpDecoder.clientDecoder())
                .addLast(FrpEncoder.basicMsgEncoder())
                .addLast(FrpEncoder.dispatchEncoder());

        pipeline.addLast(new AuthResponseHandler(clientConfig.getListeningConfigMap()));

    }

}
