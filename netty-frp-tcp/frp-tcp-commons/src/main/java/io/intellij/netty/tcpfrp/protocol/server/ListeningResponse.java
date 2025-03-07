package io.intellij.netty.tcpfrp.protocol.server;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    // 监听状态
    private Map<Integer, Boolean> listeningStatus;
}
