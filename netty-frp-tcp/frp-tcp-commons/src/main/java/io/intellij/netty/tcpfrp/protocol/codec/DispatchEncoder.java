package io.intellij.netty.tcpfrp.protocol.codec;

import io.intellij.netty.tcpfrp.protocol.channel.DispatchPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

import static io.intellij.netty.tcpfrp.protocol.FrpMsgType.DATA_PACKET;

/**
 * DispatchEncoder
 * <p>
 * Encodes a DispatchPacket into bytes for transmission over the network.
 * <p>
 * DispatchPacket = [type | dispatchId | len byte_buf]
 * <p>
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
final class DispatchEncoder extends MessageToByteEncoder<DispatchPacket> {

    @Override
    protected void encode(ChannelHandlerContext ctx, @NotNull DispatchPacket msg, @NotNull ByteBuf out) throws Exception {
        // type
        out.writeByte(DATA_PACKET.getType());

        // dispatchId
        out.writeBytes(msg.getDispatchId().getBytes(StandardCharsets.UTF_8));

        // len
        int length = msg.getPacket().readableBytes();
        out.writeInt(length);

        // data
        ByteBuf packet = msg.getPacket();
        out.writeBytes(packet);
    }

}
