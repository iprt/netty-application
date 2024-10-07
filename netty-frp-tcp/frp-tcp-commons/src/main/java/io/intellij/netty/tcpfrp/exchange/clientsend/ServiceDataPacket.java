package io.intellij.netty.tcpfrp.exchange.clientsend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ServiceDataPacket
 *
 * @author tech@intellij.io
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
public class ServiceDataPacket {
    private String serviceChannelId;
    private String userChannelId;
    private byte[] packet;
}
