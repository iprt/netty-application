package io.intellij.netty.tcpfrp.protocol;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DataPacket
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DataPacket {
    public static final int ID_LENGTH = 60;

    private String userId;
    private String serviceId;
    private ByteBuf packet;

    public static DataPacket create(String userId, String serviceId, ByteBuf packet) {
        return new DataPacket(userId, serviceId, packet);
    }

}
