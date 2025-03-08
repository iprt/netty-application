package io.intellij.netty.tcpfrp.protocol.codec;

import io.intellij.netty.tcpfrp.protocol.FrpMsgType;
import io.intellij.netty.tcpfrp.protocol.channel.DispatchIdUtils;
import io.intellij.netty.tcpfrp.protocol.channel.DispatchPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

/**
 * DispatchEncoder
 * <p>
 * Encodes a DispatchPacket into bytes for transmission over the network.
 * <p>
 * DispatchPacket = [type | len | dispatchId + data]
 * <p>
 * len = id.len + data.len
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
final class DispatchEncoder extends MessageToByteEncoder<DispatchPacket> {

    @Override
    protected void encode(ChannelHandlerContext ctx, DispatchPacket msg, ByteBuf out) throws Exception {
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
