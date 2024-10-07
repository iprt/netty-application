package io.intellij.netty.tcpfrp.exchange.clientsend;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ServiceBreakConn
 *
 * @author tech@intellij.io
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
public class ServiceBreakConn {
    private ListeningConfig listeningConfig;
    private String userChannelId;
    private String serviceChannelId;
}
