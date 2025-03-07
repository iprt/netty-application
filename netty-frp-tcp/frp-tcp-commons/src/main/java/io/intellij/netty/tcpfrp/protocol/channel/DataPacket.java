package io.intellij.netty.tcpfrp.protocol.channel;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

/**
 * DataPacket
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@Getter
public class DataPacket {

    private final String dispatchId;
    private final ByteBuf packet;

    private DataPacket(String dispatchId, ByteBuf packet) {
        this.dispatchId = dispatchId;
        this.packet = packet;
    }

    public static DataPacket create(String dispatchId, ByteBuf packet) {
        return new DataPacket(dispatchId, packet);
    }

    public static DataPacket createAndRetain(String dispatchId, ByteBuf packet) {
        return new DataPacket(dispatchId, packet.retain());
    }

}
