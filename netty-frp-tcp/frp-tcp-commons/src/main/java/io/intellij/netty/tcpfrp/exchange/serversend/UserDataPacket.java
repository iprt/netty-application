package io.intellij.netty.tcpfrp.exchange.serversend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ReadUserData
 *
 * @author tech@intellij.io
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
public class UserDataPacket {
    private String userChannelId;
    private String serviceChannelId;
    private byte[] packet;
}
