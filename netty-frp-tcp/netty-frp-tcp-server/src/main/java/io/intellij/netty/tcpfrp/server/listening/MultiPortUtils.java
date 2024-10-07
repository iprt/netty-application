package io.intellij.netty.tcpfrp.server.listening;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.exchange.serversend.ConnLocalResp;
import io.intellij.netty.utils.NetworkUtils;

import java.util.List;

/**
 * ListeningServerManager
 *
 * @author tech@intellij.io
 */
public class MultiPortUtils {

    public static ConnLocalResp connLocalResp(List<ListeningConfig> listeningConfigs) {
        for (ListeningConfig listeningConfig : listeningConfigs) {
            boolean portOpen = NetworkUtils.isPortOpen("127.0.0.1", listeningConfig.getRemotePort());
            if (portOpen) {
                return ConnLocalResp.builder()
                        .success(false).listeningConfigs(listeningConfigs)
                        .build();
            }

        }
        return ConnLocalResp.builder().success(true).listeningConfigs(listeningConfigs).build();
    }

}
