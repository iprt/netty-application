package io.intellij.netty.tcpfrp.exchange.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

import static io.intellij.netty.tcpfrp.exchange.SysConfig.ENABLE_RANDOM_TYPE;
import static io.intellij.netty.tcpfrp.exchange.codec.ExchangeProtocol.FIXED_CHANNEL_ID_LEN;
import static io.intellij.netty.tcpfrp.exchange.codec.ExchangeType.C2S_SERVICE_DATA_PACKET;
import static io.intellij.netty.tcpfrp.exchange.codec.ExchangeType.S2C_USER_DATA_PACKET;
import static io.intellij.netty.tcpfrp.exchange.codec.ExchangeType.decodeRandomToReal;
import static io.intellij.netty.tcpfrp.exchange.codec.ExchangeType.parseType;

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
        byte typeByte = in.readByte();
        ExchangeType exchangeType = parseType(ENABLE_RANDOM_TYPE ? decodeRandomToReal(typeByte) : typeByte);

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

        if ((S2C_USER_DATA_PACKET == exchangeType || C2S_SERVICE_DATA_PACKET == exchangeType) && !dataPacketUseJson) {

            if (bodyLen < 120) {
                log.error("data packet bodyLen must > 120");
                ctx.close();
            }

            byte[] userChannelIdBytes = new byte[FIXED_CHANNEL_ID_LEN];
            in.readBytes(userChannelIdBytes);

            byte[] serviceChannelIdBytes = new byte[FIXED_CHANNEL_ID_LEN];
            in.readBytes(serviceChannelIdBytes);

            String userChannelId = new String(userChannelIdBytes);
            String serviceChannelId = new String(serviceChannelIdBytes);

            // important 计算剩余的字节数
            int packetLen = bodyLen - FIXED_CHANNEL_ID_LEN * 2;
            out.add(ExchangeProtocolDataPacket.of(exchangeType, userChannelId, serviceChannelId,

                            in.retainedSlice(in.readerIndex(), packetLen)
                    )
            );
            // 更新读指针位置
            in.skipBytes(packetLen);
            // in.readBytes(in.readableBytes())
            // tips: in.retainedSlice(in.readerIndex(), in.readableBytes()) 和  in.skipBytes(in.readableBytes()) 组合使用
            return;

        }

        byte[] body = new byte[bodyLen];
        in.readBytes(body);
        out.add(new ExchangeProtocol(exchangeType, exchangeType.getClazz().getName(), body));

    }

}
