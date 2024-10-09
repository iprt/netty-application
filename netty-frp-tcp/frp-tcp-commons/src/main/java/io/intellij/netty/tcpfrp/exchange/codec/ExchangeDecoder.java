package io.intellij.netty.tcpfrp.exchange.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Slf4j
public class ExchangeDecoder extends ByteToMessageDecoder {
    private final boolean dataPacketUseJson;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        in.markReaderIndex();
        ExchangeType exchangeType = ExchangeType.getExchangeType(in.readByte());
        if (Objects.isNull(exchangeType)) {
            log.error("unknown exchange exchangeType");
            ctx.close();
            return;
        }

        if (in.readableBytes() < 4) {
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

        if ((ExchangeType.S2C_USER_DATA_PACKET == exchangeType || ExchangeType.C2S_SERVICE_DATA_PACKET == exchangeType)
            && !dataPacketUseJson) {
            this.decodeDataPacket(exchangeType, in, out);
            return;
        }

        byte[] body = new byte[bodyLen];
        in.readBytes(body);
        out.add(new ExchangeProtocol(exchangeType, exchangeType.getClazz().getName(), body));

    }

    private void decodeDataPacket(ExchangeType exchangeType, ByteBuf in, List<Object> out) {
        // TODO bodyLen 一定要大于 FIXED_CHANNEL_ID_LEN * 2
        byte[] userChannelIdBytes = new byte[ExchangeProtocolUtils.FIXED_CHANNEL_ID_LEN];
        byte[] serviceChannelIdBytes = new byte[ExchangeProtocolUtils.FIXED_CHANNEL_ID_LEN];

        in.readBytes(userChannelIdBytes);
        in.readBytes(serviceChannelIdBytes);
        String userChannelId = new String(userChannelIdBytes);
        String serviceChannelId = new String(serviceChannelIdBytes);

        // 剩余的字节就是 真实的数据包
        out.add(new ExchangeProtocolDataPacket(exchangeType, userChannelId, serviceChannelId,
                        // in.readBytes(in.readableBytes())
                        in.retainedSlice(in.readerIndex(), in.readableBytes())
                )
        );
        //  in.retainedSlice(in.readerIndex(), in.readableBytes()) 和  in.skipBytes(in.readableBytes()) 组合使用
        in.skipBytes(in.readableBytes());
    }

}
