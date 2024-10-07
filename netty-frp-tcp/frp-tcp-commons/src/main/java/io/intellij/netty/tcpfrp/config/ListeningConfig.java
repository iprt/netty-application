package io.intellij.netty.tcpfrp.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ListeningConfig
 *
 * @author tech@intellij.io
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
public class ListeningConfig {
    private String name;
    private String localIp;
    private int localPort;
    private int remotePort;
}
