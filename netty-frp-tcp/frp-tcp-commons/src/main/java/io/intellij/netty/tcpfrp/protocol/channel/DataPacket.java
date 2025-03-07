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
    public static final int ID_LENGTH = 60;

    private final String userId;
    private final String serviceId;
    private final ByteBuf packet;

    private DataPacket(String userId, String serviceId, ByteBuf packet) {
        this.userId = userId;
        this.serviceId = serviceId;
        this.packet = packet;
    }

    public static DataPacket create(String userId, String serviceId, ByteBuf packet) {
        return new DataPacket(userId, serviceId, packet);
    }

    public static DataPacket createAndRetain(String userId, String serviceId, ByteBuf packet) {
        return new DataPacket(userId, serviceId, packet.retain());
    }

}
