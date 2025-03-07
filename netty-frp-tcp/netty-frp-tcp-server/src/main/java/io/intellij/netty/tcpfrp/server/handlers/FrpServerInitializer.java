package io.intellij.netty.tcpfrp.server.handlers;

import io.intellij.netty.tcpfrp.config.ServerConfig;
import io.intellij.netty.tcpfrp.protocol.codec.DataPacketEncoder;
import io.intellij.netty.tcpfrp.protocol.codec.FrpBasicMsgEncoder;
import io.intellij.netty.tcpfrp.protocol.codec.FrpServerDecoder;
import io.intellij.netty.tcpfrp.server.handlers.initial.ServerAuthHandler;
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
    private final ServerConfig serverConfig;

    @Override
    protected void initChannel(@NotNull SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new FrpServerDecoder())
                .addLast("FrpBasicMsgEncoder", new FrpBasicMsgEncoder())
                .addLast("DataPacketEncoder", new DataPacketEncoder());

        pipeline.addLast(new ServerAuthHandler(serverConfig.getAuthToken()));
    }

}
