package io.intellij.netty.tcpfrp.exchange.codec;

import io.intellij.netty.tcpfrp.exchange.ExchangeProtocol;
import io.intellij.netty.tcpfrp.exchange.ExchangeType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * ExchangeDecoder
 * <p>
 * 解码器
 *
 * @author tech@intellij.io
 */
@Slf4j
public class ExchangeDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        byteBuf.markReaderIndex();
        ExchangeType type = ExchangeType.getType(byteBuf.readByte());
        if (Objects.isNull(type)) {
            log.error("unknown exchange type");
            ctx.close();
        }

        int classLen = byteBuf.readByte();

        if (classLen == 0) {
            log.error("class name length must > 0");
            ctx.close();
            return;
        }

        int i = byteBuf.readableBytes();
        if (i < classLen) {
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] classNameBytes = new byte[classLen];
        byteBuf.readBytes(classNameBytes);
        String className = new String(classNameBytes, StandardCharsets.UTF_8);

        int bodyLen = byteBuf.readInt();
        if (bodyLen == 0) {
            log.error("body length must > 0");
            ctx.close();
            return;
        }

        int j = byteBuf.readableBytes();
        if (j < bodyLen) {
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] body = new byte[bodyLen];
        byteBuf.readBytes(body);

        list.add(ExchangeProtocol.builder()
                .exchangeType(type)
                .classLen(classLen).className(className)
                .bodyLen(bodyLen).body(body)
                .build());

    }

}
