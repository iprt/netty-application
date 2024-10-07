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
public class GetUserData {
    private String userChannelId;
    private String serviceChannelId;
    private byte[] data;
}
