package io.intellij.netty.tcpfrp.protocol.codec.encoder;

import io.netty.channel.ChannelOutboundHandler;

/**
 * FrpEncoder
 *
 * @author tech@intellij.io
 * @since 2025-03-07
 */
public final class FrpEncoder {
    private FrpEncoder() {
    }

    /**
     * Creates and returns a ChannelOutboundHandler instance for encoding basic messages.
     * The returned encoder translates the basic message into its corresponding byte representation
     * following the format: type|length|json.
     *
     * @return a ChannelOutboundHandler for encoding basic messages.
     */
    public static ChannelOutboundHandler basicMsgEncoder() {
        return new FrpBasicMsgEncoder();
    }

    /**
     * Creates and returns a ChannelOutboundHandler instance specifically for encoding DispatchPacket messages.
     * The returned encoder translates DispatchPackets into their corresponding byte representations
     * following the format: type | length | dispatchId + data, where length equals the sum of the lengths
     * of dispatchId and data.
     *
     * @return a ChannelOutboundHandler for encoding DispatchPacket messages.
     */
    public static ChannelOutboundHandler dispatchEncoder() {
        return new DispatchEncoder();
    }

}
