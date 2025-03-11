package io.intellij.netty.tcpfrp.protocol.codec;

import io.intellij.netty.tcpfrp.protocol.channel.DispatchPacket;
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

    /**
     * Creates a new instance of {@link ChannelInboundHandler} configured for client-side decoding of FRP messages.
     * The decoder processes incoming messages in client mode, interpreting specific message types and formats.
     *
     * @return a {@link ChannelInboundHandler} instance for decoding messages in client mode
     */
    public static @NotNull ChannelInboundHandler clientDecoder() {
        return new FrpDecoder(FrpDecoder.MODE.CLIENT);
    }

    /**
     * Creates a new instance of {@link ChannelInboundHandler} configured for server-side decoding of FRP messages.
     * The decoder processes incoming messages in server mode, interpreting specific message types and formats.
     *
     * @return a {@link ChannelInboundHandler} instance for decoding messages in server mode
     */
    public static @NotNull ChannelInboundHandler serverDecoder() {
        return new FrpDecoder(FrpDecoder.MODE.SERVER);
    }

    /**
     * Creates a new instance of {@link ChannelOutboundHandler} for encoding basic FRP messages.
     * The encoder is responsible for serializing basic message types into the specified protocol format
     * before transmission over the network.
     *
     * @return a {@link ChannelOutboundHandler} instance for encoding basic FRP messages
     */
    public static @NotNull ChannelOutboundHandler basicMsgEncoder() {
        return new FrpBasicMsgEncoder();
    }

    /**
     * Creates a new instance of {@link ChannelOutboundHandler} for encoding DispatchPackets.
     * The encoder serializes a {@link DispatchPacket} into the appropriate byte format, which consists of:
     * type, dispatch ID, length, and the byte buffer of the dispatch packet.
     *
     * @return a {@link ChannelOutboundHandler} instance for encoding DispatchPackets
     */
    public static @NotNull ChannelOutboundHandler dispatchEncoder() {
        return new DispatchEncoder();
    }

}
