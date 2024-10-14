package io.intellij.netty.tcpfrp.exchange.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static io.intellij.netty.tcpfrp.exchange.SystemConfig.ENABLE_RANDOM_TYPE;
import static io.intellij.netty.tcpfrp.exchange.codec.ExchangeType.encodeRandom;

/**
 * ExchangeProtocolEncoder
 *
 * @author tech@intellij.io
 */
public class ExchangeEncoder extends MessageToByteEncoder<ExchangeProtocol> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ExchangeProtocol exchangeProtocol, ByteBuf byteBuf) throws Exception {
        ExchangeType type = exchangeProtocol.exchangeType();
        // 1 byte
        byteBuf.writeByte(ENABLE_RANDOM_TYPE ? encodeRandom(type.getType()) : type.getType());

        byte[] bodyBytes = exchangeProtocol.body();
        int bodyLen = bodyBytes.length;
        byteBuf.writeInt(bodyLen);
        byteBuf.writeBytes(bodyBytes);
    }
}