package io.intellij.netty.tcpfrp.protocol.client;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.protocol.FrpBasicMsg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

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
    private Map<String, ListeningConfig> configMap;

    public static FrpBasicMsg create(Map<String, ListeningConfig> configMap) {
        return FrpBasicMsg.createListeningRequest(new ListeningRequest(configMap));
    }

}
