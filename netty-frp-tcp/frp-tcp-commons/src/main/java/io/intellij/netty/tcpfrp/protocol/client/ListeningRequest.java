package io.intellij.netty.tcpfrp.protocol.client;

import io.intellij.netty.tcpfrp.protocol.FrpBasicMsg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ListeningRequest
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ListeningRequest {
    // private Map<String, ListeningConfig> configMap;
    private List<Integer> listeningPorts;

    public static FrpBasicMsg create(List<Integer> listeningPorts) {
        return FrpBasicMsg.createListeningRequest(new ListeningRequest(listeningPorts));
    }

}
