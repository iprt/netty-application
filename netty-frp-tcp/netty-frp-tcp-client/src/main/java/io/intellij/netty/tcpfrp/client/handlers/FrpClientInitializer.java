package io.intellij.netty.tcpfrp.client.handlers;

import io.intellij.netty.tcpfrp.client.handlers.initial.ClientAuthResponseHandler;
import io.intellij.netty.tcpfrp.config.ClientConfig;
import io.intellij.netty.tcpfrp.protocol.codec.DataPacketEncoder;
import io.intellij.netty.tcpfrp.protocol.codec.FrpBasicMsgEncoder;
import io.intellij.netty.tcpfrp.protocol.codec.FrpClientDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.RequiredArgsConstructor;

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
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // 编解码器
        pipeline.addLast("FrpClientDecoder", new FrpClientDecoder())
                .addLast("FrpBasicMsgEncoder", new FrpBasicMsgEncoder())
                .addLast("DataPacketEncoder", new DataPacketEncoder());

        // 处理器
        pipeline.addLast(new ClientAuthResponseHandler(clientConfig.getListeningConfigMap()));

    }

}
