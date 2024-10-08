package io.intellij.netty.tcpfrp.exchange.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

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
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> list) throws Exception {
        in.markReaderIndex();
        ExchangeType exchangeType = ExchangeType.getExchangeType(in.readByte());
        if (Objects.isNull(exchangeType)) {
            log.error("unknown exchange exchangeType");
            ctx.close();
            return;
        }

        if (in.readableBytes() < 4) {
            // 恢复读指针位置
            in.resetReaderIndex();
            return;
        }

        int bodyLen = in.readInt();
        if (bodyLen == 0) {
            log.error("body length must > 0");
            ctx.close();
            return;
        }

        if (in.readableBytes() < bodyLen) {
            in.resetReaderIndex();
            return;
        }

        byte[] body = new byte[bodyLen];
        in.readBytes(body);

        list.add(ExchangeProtocol.builder()
                .exchangeType(exchangeType).className(exchangeType.getClazz().getName())
                .body(body)
                .build());

    }

}
