package io.intellij.netty.tcpfrp.exchange.both;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * DataPacket
 *
 * @author tech@intellij.io
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
public class DataPacket {
    private String userChannelId;
    private String serviceChannelId;
    private byte[] packet;

    private String from;
}
