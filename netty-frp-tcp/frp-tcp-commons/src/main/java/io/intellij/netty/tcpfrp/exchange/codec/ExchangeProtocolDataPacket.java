package io.intellij.netty.tcpfrp.exchange.codec;

import io.netty.buffer.ByteBuf;

/**
 * ExchangeProtocolDataPacket
 *
 * @author tech@intellij.io
 */
public record ExchangeProtocolDataPacket(ExchangeType exchangeType,
                                         String userChannelId, String serviceChannelId,
                                         ByteBuf packet) {

    public static ExchangeProtocolDataPacket of(ExchangeType exchangeType, String userChannelId, String serviceChannelId, ByteBuf packet) {
        return new ExchangeProtocolDataPacket(exchangeType, userChannelId, serviceChannelId, packet);
    }

}