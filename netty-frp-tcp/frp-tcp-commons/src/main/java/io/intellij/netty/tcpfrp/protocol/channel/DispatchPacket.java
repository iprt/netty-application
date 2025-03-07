package io.intellij.netty.tcpfrp.protocol.channel;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

/**
 * DispatchPacket
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@Getter
public class DispatchPacket {

    private final String dispatchId;
    private final ByteBuf packet;

    private DispatchPacket(String dispatchId, ByteBuf packet) {
        this.dispatchId = dispatchId;
        this.packet = packet;
    }

    public static DispatchPacket create(String dispatchId, ByteBuf packet) {
        return new DispatchPacket(dispatchId, packet);
    }

    public static DispatchPacket createAndRetain(String dispatchId, ByteBuf packet) {
        return new DispatchPacket(dispatchId, packet.retain());
    }

}
