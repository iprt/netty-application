package io.intellij.netty.tcpfrp.protocol.server;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * ListeningResponse
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
public class ListeningResponse {
    // 是否连接成功
    private boolean success;
    // 失败原因
    private String reason;

    // 连接的配置
    private List<ListeningConfig> listeningConfigs;
    // 监听状态
    private Map<Integer, Boolean> listeningStatus;
}
