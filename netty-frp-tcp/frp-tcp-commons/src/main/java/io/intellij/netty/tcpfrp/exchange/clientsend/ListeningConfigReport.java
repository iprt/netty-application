package io.intellij.netty.tcpfrp.exchange.clientsend;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

/**
 * ReportListeningConfig
 *
 * @author tech@intellij.io
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
public class ListeningConfigReport {
    private Map<String, ListeningConfig> listeningConfigMap;
}
