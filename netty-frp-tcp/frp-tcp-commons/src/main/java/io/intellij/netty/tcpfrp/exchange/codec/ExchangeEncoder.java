package io.intellij.netty.tcpfrp.exchange.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * ExchangeProtocolEncoder
 *
 * @author tech@intellij.io
 */
public class ExchangeEncoder extends MessageToByteEncoder<ExchangeProtocol> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ExchangeProtocol exchangeProtocol, ByteBuf byteBuf) throws Exception {
        ExchangeType type = exchangeProtocol.getExchangeType();
        // 1 byte
        byteBuf.writeByte(type.getType());

        byte[] bodyBytes = exchangeProtocol.getBody();
        int bodyLen = bodyBytes.length;
        byteBuf.writeInt(bodyLen);
        byteBuf.writeBytes(bodyBytes);

    }
}

/*

| 1byte | 4byte len | ... msg |

 */