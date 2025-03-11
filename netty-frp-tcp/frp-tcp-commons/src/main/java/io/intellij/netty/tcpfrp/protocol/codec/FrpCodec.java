package io.intellij.netty.tcpfrp.protocol.codec;

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
        return new FrpDecoder(FrpDecoder.MODE.CLIENT);
    }

    public static @NotNull ChannelInboundHandler serverDecoder() {
        return new FrpDecoder(FrpDecoder.MODE.SERVER);
    }

    public static @NotNull ChannelOutboundHandler basicMsgEncoder() {
        return new FrpBasicMsgEncoder();
    }

    public static @NotNull ChannelOutboundHandler dispatchEncoder() {
        return new DispatchEncoder();
    }

}
