package io.intellij.netty.tcpfrp.server.listening;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.protocol.server.ListeningResponse;
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

    public static ListeningResponse test(List<ListeningConfig> listeningConfigs) {
        Map<Integer, Boolean> listeningStatus = new HashMap<>();
        for (ListeningConfig config : listeningConfigs) {
            int remotePort = config.getRemotePort();
            boolean portInUse = ServerSocketUtils.isPortInUse(remotePort);
            listeningStatus.put(remotePort, portInUse);
        }
        return ListeningResponse.builder()
                .success(listeningStatus.values().stream().noneMatch(b -> b))
                .listeningConfigs(listeningConfigs)
                .listeningStatus(listeningStatus).build();
    }

}
