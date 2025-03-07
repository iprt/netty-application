package io.intellij.netty.tcpfrp.client.handlers;

import io.intellij.netty.tcpfrp.client.handlers.initial.ClientAuthResponseHandler;
import io.intellij.netty.tcpfrp.config.ClientConfig;
import io.intellij.netty.tcpfrp.protocol.codec.FrpDecoder;
import io.intellij.netty.tcpfrp.protocol.codec.FrpEncoder;
import io.netty.channel.ChannelInitializer;
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
public class FrpClientInitializer extends ChannelInitializer<SocketChannel> {
    private final ClientConfig clientConfig;

    @Override
    protected void initChannel(@NotNull SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // 编解码器
        pipeline.addLast(FrpDecoder.clientDecoder())
                .addLast(FrpEncoder.basicMsgEncoder())
                .addLast(FrpEncoder.dispatchEncoder());

        // 处理器
        pipeline.addLast(new ClientAuthResponseHandler(clientConfig.getListeningConfigMap()));

    }

}
