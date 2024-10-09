package io.intellij.netty.tcpfrp.server.listening;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.exchange.s2c.ListeningLocalResp;
import io.intellij.netty.utils.ServerSocketUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MultiPortUtils
 *
 * @author tech@intellij.io
 */
public class MultiPortUtils {

    public static ListeningLocalResp testLocalListing(List<ListeningConfig> listeningConfigs) {
        Map<Integer, Boolean> listeningStatus = new HashMap<>();
        for (ListeningConfig listeningConfig : listeningConfigs) {
            int remotePort = listeningConfig.getRemotePort();
            boolean portInUse = ServerSocketUtils.isPortInUse(remotePort);
            listeningStatus.put(remotePort, portInUse);
        }
        return ListeningLocalResp.builder()
                .success(listeningStatus.values().stream().noneMatch(b -> b))
                .listeningConfigs(listeningConfigs)
                .listeningStatus(listeningStatus).build();
    }

}
