package io.intellij.netty.tcpfrp.exchange.s2c;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * ListeningLocalResp
 *
 * @author tech@intellij.io
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
public class ListeningLocalResp {
    // 是否连接成功
    private boolean success;
    // 连接的配置
    private List<ListeningConfig> listeningConfigs;

    private Map<Integer, Boolean> listeningStatus;
}
