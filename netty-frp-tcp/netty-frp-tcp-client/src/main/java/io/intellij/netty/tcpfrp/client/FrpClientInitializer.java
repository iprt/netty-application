package io.intellij.netty.tcpfrp.client;

import io.intellij.netty.tcpfrp.client.handlers.AuthTokenHandler;
import io.intellij.netty.tcpfrp.config.ClientConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * FrpClientInitializer
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
public class FrpClientInitializer extends ChannelInitializer<SocketChannel> {
    private final int tokenLen = UUID.randomUUID().toString().length();

    private final ClientConfig clientConfig;

    @Override
    protected void initChannel(@NotNull SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        pipeline.addLast("FixedLengthFrameDecoder", new FixedLengthFrameDecoder(tokenLen));
        pipeline.addLast("StringDecoder", new StringDecoder(StandardCharsets.UTF_8));
        pipeline.addLast("StringEncoder", new StringEncoder(StandardCharsets.UTF_8));
        pipeline.addLast(AuthTokenHandler.HANDLER_NAME,
                new AuthTokenHandler(clientConfig.getServerConfig().getAuthToken(), clientConfig.getListeningConfigMap()));

    }

}
