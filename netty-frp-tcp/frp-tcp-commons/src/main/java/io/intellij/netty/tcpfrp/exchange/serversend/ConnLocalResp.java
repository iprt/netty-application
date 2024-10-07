package io.intellij.netty.tcpfrp.exchange.serversend;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * ConnLocalResp
 *
 * @author tech@intellij.io
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
public class ConnLocalResp {

    // 是否连接成功
    private boolean success;

    // 连接的配置
    private List<ListeningConfig> listeningConfigs;


}
