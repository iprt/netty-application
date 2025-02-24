package io.intellij.netty.tcp.lb.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

/**
 * LbConfig
 *
 * @author tech@intellij.io
 * @since 2025-02-20
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
public class LbConfig {
    private int port;
    private LbStrategy lbStrategy;
    private Map<String, Backend> backends;
}
