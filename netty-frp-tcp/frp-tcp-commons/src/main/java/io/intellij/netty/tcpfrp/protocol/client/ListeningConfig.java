package io.intellij.netty.tcpfrp.protocol.client;

import lombok.AllArgsConstructor;
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
@Data
public class ListeningConfig {
    private String name;
    private String localIp;
    private int localPort;
    private int remotePort;

    public static ListeningConfig create(String name, String localIp, int localPort, int remotePort) {
        return new ListeningConfig(name, localIp, localPort, remotePort);
    }
}
