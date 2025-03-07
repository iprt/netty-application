package io.intellij.netty.tcpfrp.protocol.codec;

import io.intellij.netty.tcpfrp.protocol.FrpMsgType;
import io.intellij.netty.tcpfrp.protocol.channel.DataPacket;
import io.intellij.netty.tcpfrp.protocol.channel.DispatchIdUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

/**
 * DataPacketEncoder
 * <p>
 * Encodes a DataPacket into bytes for transmission over the network.
 * <p>
 * type | len | dispatchId + data
 * <p>
 * len = id.len + data.len
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
        int length = DispatchIdUtils.ID_LENGTH + msg.getPacket().readableBytes();
        out.writeInt(length);

        // data
        out.writeBytes(msg.getDispatchId().getBytes(StandardCharsets.UTF_8));

        ByteBuf packet = msg.getPacket();
        out.writeBytes(packet);
    }

}
