package io.intellij.netty.tcpfrp.server.handlers;

import io.intellij.netty.tcpfrp.config.ServerConfig;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.UUID;

/**
 * FrpServerInitializer
 *
 * @author tech@intellij.io
 */
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class FrpServerInitializer extends ChannelInitializer<SocketChannel> {
    private final int TOKEN_LEN = UUID.randomUUID().toString().length();

    private final ServerConfig serverConfig;

    @Override
    protected void initChannel(@NotNull SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        pipeline.addLast("FixedLengthFrameDecoder", new FixedLengthFrameDecoder(TOKEN_LEN));
        pipeline.addLast("StringEncoder", new StringEncoder(Charset.defaultCharset()));
        pipeline.addLast("StringDecoder", new StringDecoder(Charset.defaultCharset()));
        pipeline.addLast(AuthTokenHandler.HANDLER_NAME, new AuthTokenHandler(serverConfig.getAuthToken()));

    }

}
