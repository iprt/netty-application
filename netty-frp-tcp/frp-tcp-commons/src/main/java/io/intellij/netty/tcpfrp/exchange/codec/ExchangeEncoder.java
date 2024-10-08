package io.intellij.netty.tcpfrp.exchange.codec;

import io.intellij.netty.tcpfrp.exchange.ExchangeProtocol;
import io.intellij.netty.tcpfrp.exchange.ExchangeType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

/**
 * ExchangeProtocolEncoder
 *
 * @author tech@intellij.io
 */
public class ExchangeEncoder extends MessageToByteEncoder<ExchangeProtocol> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ExchangeProtocol exchangeProtocol, ByteBuf byteBuf) throws Exception {
        ExchangeType type = exchangeProtocol.getExchangeType();
        // 只要一个字节
        byteBuf.writeByte(type.getType());

        int classLen = exchangeProtocol.getClassLen();
        byteBuf.writeByte(classLen);
        byteBuf.writeBytes(exchangeProtocol.getClassName().getBytes(StandardCharsets.UTF_8));

        int bodyLen = exchangeProtocol.getBodyLen();
        byteBuf.writeInt(bodyLen);
        byteBuf.writeBytes(exchangeProtocol.getBody());

    }
}
