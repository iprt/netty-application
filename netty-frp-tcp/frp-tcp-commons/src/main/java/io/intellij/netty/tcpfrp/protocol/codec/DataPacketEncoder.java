package io.intellij.netty.tcpfrp.protocol.codec;

import io.intellij.netty.tcpfrp.protocol.DataPacket;
import io.intellij.netty.tcpfrp.protocol.FrpMsgType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

/**
 * DataPacketEncoder
 * <p>
 * Encodes a DataPacket into bytes for transmission over the network.
 * <p>
 * type | len | (userId | serviceId | data)
 * <p>
 * len = id.len * 2 + data.len
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
public class DataPacketEncoder extends MessageToByteEncoder<DataPacket> {

    @Override
    protected void encode(ChannelHandlerContext ctx, DataPacket msg, ByteBuf out) throws Exception {
        // type
        out.writeByte(FrpMsgType.DATA_PACKET.getType());

        // len
        int length = DataPacket.ID_LENGTH * 2 + msg.getPacket().readableBytes();
        out.writeInt(length);

        // data
        out.writeBytes(msg.getUserId().getBytes(StandardCharsets.UTF_8));
        out.writeBytes(msg.getServiceId().getBytes(StandardCharsets.UTF_8));

        ByteBuf packet = msg.getPacket();
        out.writeBytes(packet);
    }

}
