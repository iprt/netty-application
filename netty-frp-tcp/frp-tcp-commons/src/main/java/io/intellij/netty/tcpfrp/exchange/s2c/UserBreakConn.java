package io.intellij.netty.tcpfrp.exchange.s2c;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * UserBreakConn
 * <p>
 * 用户断开连接
 *
 * @author tech@intellij.io
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
public class UserBreakConn {
    private ListeningConfig listeningConfig;
    private String userChannelId;
    private String serviceChannelId;
}
