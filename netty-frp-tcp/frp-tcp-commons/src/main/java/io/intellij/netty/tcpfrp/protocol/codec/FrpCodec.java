package io.intellij.netty.tcpfrp.protocol.codec;

import io.intellij.netty.tcpfrp.protocol.codec.decoder.FrpDecoder;
import io.intellij.netty.tcpfrp.protocol.codec.encoder.FrpEncoder;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import org.jetbrains.annotations.NotNull;

/**
 * FrpCodec
 *
 * @author tech@intellij.io
 * @since 2025-03-08
 */
public class FrpCodec {

    public static @NotNull ChannelInboundHandler clientDecoder() {
        return FrpDecoder.clientDecoder();
    }

    public static @NotNull ChannelInboundHandler serverDecoder() {
        return FrpDecoder.serverDecoder();
    }

    public static @NotNull ChannelOutboundHandler basicMsgEncoder() {
        return FrpEncoder.basicMsgEncoder();
    }

    public static @NotNull ChannelOutboundHandler dispatchEncoder() {
        return FrpEncoder.dispatchEncoder();
    }

}
