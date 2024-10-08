package io.intellij.netty.tcpfrp.exchange.s2c;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * UserConn
 *
 * @author tech@intellij.io
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
public class UserCreateConn {
    private ListeningConfig listeningConfig;
    private String userChannelId;
}
