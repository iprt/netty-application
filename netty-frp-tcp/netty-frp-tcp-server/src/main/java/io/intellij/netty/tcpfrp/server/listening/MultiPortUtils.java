package io.intellij.netty.tcpfrp.server.listening;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.exchange.serversend.ListeningLocalResp;
import io.intellij.netty.utils.SocketUtils;

import java.util.List;

/**
 * MultiPortUtils
 *
 * @author tech@intellij.io
 */
public class MultiPortUtils {

    public static ListeningLocalResp connLocalResp(List<ListeningConfig> listeningConfigs) {
        for (ListeningConfig listeningConfig : listeningConfigs) {
            boolean portOpen = SocketUtils.isPortOpen("127.0.0.1", listeningConfig.getRemotePort());
            if (portOpen) {
                return ListeningLocalResp.builder()
                        .success(false).listeningConfigs(listeningConfigs)
                        .build();
            }
        }
        return ListeningLocalResp.builder().success(true).listeningConfigs(listeningConfigs).build();
    }

}
